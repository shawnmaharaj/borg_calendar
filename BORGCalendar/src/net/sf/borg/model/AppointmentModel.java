/*
 This file is part of BORG.

 BORG is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 BORG is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with BORG; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Copyright 2003 by Mike Berger
 */

package net.sf.borg.model;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.common.XTree;
import net.sf.borg.model.CategoryModel.CategorySource;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.model.beans.AppointmentXMLAdapter;
import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.jdbc.ApptJdbcDB;
import net.sf.borg.model.undo.AppointmentUndoItem;
import net.sf.borg.model.undo.UndoLog;

public class AppointmentModel extends Model implements Model.Listener,
		CategorySource {

	static private AppointmentModel self_ = null;


	// return true if an appointment is skipped on a particular date
	public static boolean isSkipped(Appointment ap, Calendar cal) {
		int dk = dkey(cal);
		String sk = Integer.toString(dk);
		Vector<String> skv = ap.getSkipList();
		if (skv != null && skv.contains(sk)) {
			return true;
		}

		return false;
	}

	private BeanDB<Appointment> db_; // the SMDB database - see mdb.SMDB

	public BeanDB<Appointment> getDB() {
		return (db_);
	}

	/*
	 * map_ contains each "base" day key that has appts and maps it to a list of
	 * appt keys for that day.
	 */
	private HashMap<Integer, Collection<Integer>> map_;
	public static AppointmentModel getReference() {
		if( self_ == null )
			try {
				self_ = new AppointmentModel();
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return null;
			}
		return (self_);
	}
	
	private AppointmentModel() throws Exception
	{
		map_ = new HashMap<Integer, Collection<Integer>>();
		vacationMap_ = new HashMap<Integer, Integer>();
		db_ = new ApptJdbcDB();

		// init categories and currentcategories
		CategoryModel.getReference().addSource(this);
		CategoryModel.getReference().addListener(this);

		// scan the DB and build the appt map_
		buildMap();

	}
	

	private HashMap<Integer, Integer> vacationMap_;

	/**
	 * return a base DB key for a given day
	 */
	public static int dkey(int year, int month, int date) {
		return ((year - 1900) * 1000000 + (month + 1) * 10000 + date * 100);
	}

	public static int dkey(Calendar cal) {
		return dkey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
				.get(Calendar.DATE));
	}

	// return a key that only considers month and date
	public static int birthdayKey(int dkey) {
		return ((dkey % 1000000) * 1000000);
	}

	// get a new row from SMDB. The row will internally contain the Appt
	// schema
	public Appointment newAppt() {
		Appointment appt = db_.newObj();
		return (appt);
	}

	// assumes date has been updated
	// written to support link processing
	public void changeDate(Appointment ap) throws Exception {
		Appointment orig = ap.copy();
		saveAppt(ap, true); // sets new key
		LinkModel.getReference().moveLinks(orig, ap);
		delAppt(orig.getKey()); // removes links
		
		UndoLog.getReference().addItem(AppointmentUndoItem.recordMove(orig));
	}

	public void delAppt(int key) {
		try {
			Appointment appt = getAppt(key);
			if (appt != null)
				delAppt(appt);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	public void delAppt(Appointment appt) {
		delAppt(appt, false);
	}

	// delete a row from the database
	public void delAppt(Appointment appt, boolean undo) {

		try {

			Appointment orig_appt = getAppt(appt.getKey());

			LinkModel.getReference().deleteLinks(appt);

			db_.delete(appt.getKey());
			if (!undo) {
				UndoLog.getReference().addItem(
						AppointmentUndoItem.recordDelete(orig_appt));
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// even if delete fails - still refresh cache info
		// and tell listeners - db failure may have been due to
		// a sync causing a record already deleted error
		// this needs to be reflected in the map
		try {
			// recreate the appointment hashmap
			buildMap();

			// refresh all views that are displaying appt data from this
			// model
			refreshListeners();
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
	}

	// delete one occurrence of a repeating appt
	public void delOneOnly(int key, int rkey) {
		try {

			// read the appt row from SMDB
			Appointment appt = db_.readObj(key);

			// get the number of repeats
			Integer tms = appt.getTimes();
			if (tms == null)
				throw new Warning(Resource
						.getResourceString("Appointment_does_not_repeat"));

			// get the list of repeats that have been deleted - the SKip
			// list
			Vector<String> vect = appt.getSkipList();
			if (vect == null)
				vect = new Vector<String>();

			// add the current appt key to the SKip list
			vect.add(Integer.toString(rkey));
			appt.setSkipList(vect);
			saveAppt(appt, false);

			// if we are deleting the next todo then do it
			Date nt = appt.getNextTodo();
			if (nt == null)
				nt = appt.getDate();
			Calendar cal = new GregorianCalendar();
			cal.setTime(nt);
			if (rkey == dkey(cal)) {
				do_todo(appt.getKey(), false);
			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
	}

	public void saveAppt(Appointment r, boolean add) {
		saveAppt(r, add, false);
	}

	public void saveAppt(Appointment r, boolean add, boolean undo) {

		try {
			Appointment orig_appt = getAppt(r.getKey());
			if (add == true) {
				// get the next unused key for a given day
				// to do this, start with the "base" key for a given day.
				// then see if an appt has this key.
				// keep adding 1 until a key is found that has no appt
				GregorianCalendar gcal = new GregorianCalendar();
				gcal.setTime(r.getDate());
				int key = AppointmentModel.dkey(gcal);

				// if undo is adding back record - force the key to
				// be what is in the record
				if (!undo) {
					try {
						while (true) {
							Appointment ap = getAppt(key);
							if (ap == null)
								break;
							key++;
						}

					} catch (Exception ee) {
						Errmsg.errmsg(ee);
						return;
					}
				}

				// key is now a free key
				r.setKey(key);
				r.setNew(true);
				r.setDeleted(false);

				db_.addObj(r);
				if (!undo) {
					UndoLog.getReference().addItem(
							AppointmentUndoItem.recordAdd(r));
				}
			} else {

				r.setModified(true);
				r.setDeleted(false);

				db_.updateObj(r);
				if (!undo) {
					UndoLog.getReference().addItem(
							AppointmentUndoItem.recordUpdate(orig_appt));
				}
			}

			// update category list
			String cat = r.getCategory();
			if (cat != null && !cat.equals(""))
				CategoryModel.getReference().addCategory(cat);

		} catch (Exception e) {
			Errmsg.errmsg(e);

		}

		try {
			// recreate the appointment hashmap
			buildMap();

			// refresh all views that are displaying appt data from this
			// model
			refreshListeners();
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
	}

	// get an appt from the database by key
	public Appointment getAppt(int key) throws Exception {
		Appointment appt = db_.readObj(key);
		return (appt);
	}

	// search the appt DB using a search string and
	// create a Vector containing the results
	public Vector<Appointment> get_srch(String s, boolean case_sensitive) {

		Vector<Appointment> res = new Vector<Appointment>();

		try {

			// load all appts into appt list
			Collection<Appointment> allappts = getAllAppts();

			for (Appointment appt : allappts) {
				// read each appt

				// if category set, filter appts
				if (!CategoryModel.getReference().isShown(appt.getCategory())) {
					continue;
				}

				String tx = appt.getText();
				Date d = appt.getDate();
				if (d == null || tx == null)
					continue;

				if (case_sensitive) {
					// check if appt text contains the search string
					if (tx.indexOf(s) == -1)
						continue;
				} else {
					// check if appt text contains the search string
					String ltx = tx.toLowerCase();
					String ls = s.toLowerCase();
					if (ltx.indexOf(ls) == -1)
						continue;
				}

				// add the appt to the search results
				res.add(appt);

			}

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
		return (res);

	}

	// this function is called to mark a to do as done from the todo gui
	// window
	// the user can optionally indicate that the todo is to be deleted - but
	// we must still
	// make sure it is the last repeat if the todo repeats
	public void do_todo(int key, boolean del) throws Exception {
		// read the DB row for the ToDo
		Appointment appt = db_.readObj(key);

		// curtodo is the date of the todo that is to be "done"
		Date curtodo = appt.getNextTodo();
		Date d = appt.getDate();
		if (curtodo == null) {
			curtodo = d;
		}

		// newtodo will be the name of the next todo occurrence (if the todo
		// repeats and is not done)
		Date newtodo = null;

		Integer tms = appt.getTimes();
		String rpt = Repeat.getFreq(appt.getFrequency());

		// find next to do if it repeats by doing calendar math
		if (tms != null && tms.intValue() > 1 && rpt != null
				&& !rpt.equals(Repeat.ONCE)) {
			int tm = tms.intValue();

			Calendar ccal = new GregorianCalendar();
			Calendar ncal = new GregorianCalendar();

			// ccal is the current todo and ncal is the original appt date
			// ncal will be incremented until we find the todo after the
			// one in ccal
			ccal.setTime(curtodo);
			ncal.setTime(d);

			Repeat repeat = new Repeat(ncal, appt.getFrequency());
			for (int i = 1; i < tm; i++) {

				if (ncal != null
						&& ncal.get(Calendar.YEAR) == ccal.get(Calendar.YEAR)
						&& ncal.get(Calendar.MONTH) == ccal.get(Calendar.MONTH)
						&& ncal.get(Calendar.DATE) == ccal.get(Calendar.DATE)) {

					while (true) {
						ncal = repeat.next();
						if (ncal == null)
							break;
						if (isSkipped(appt, ncal))
							continue;

						newtodo = ncal.getTime();
						break;

					}
					// System.out.println("newtodo=" + newtodo.getTime());
					break;
				}

				ncal = repeat.next();

			}
		}

		if (newtodo != null) {
			// a next todo was found, set NT to that value
			// and don't delete the appt
			appt.setNextTodo(newtodo);
			saveAppt(appt, false);
		} else {
			// there is no next todo - shut off the todo
			// unless the user wants it deleted. if so, delete it.
			if (del) {
				delAppt(appt);
			} else {
				appt.setTodo(false);
				appt.setColor("strike");
				saveAppt(appt, false);
			}
		}

	}

	// get a list of appts for a given day key
	public List<Integer> getAppts(int key) {
		return ((List<Integer>) map_.get(new Integer(key)));
	}

	// get a vector containing all of the todo appts in the DB
	public Collection<Appointment> get_todos() {

		ArrayList<Appointment> av = new ArrayList<Appointment>();
		try {

			// iterate through appts in the DB
			AppointmentDB kf = (AppointmentDB) db_;
			Collection<Integer> keycol = kf.getTodoKeys();
			// Collection keycol = AppointmentHelper.getTodoKeys(db_);
			for (Integer ki : keycol) {
				int key = ki.intValue();

				// read the full appt from the DB and add to the vector
				Appointment appt = db_.readObj(key);
				if (appt.getDeleted())
					continue;

				// if category set, filter appts
				if (!CategoryModel.getReference().isShown(appt.getCategory())) {
					continue;
				}

				av.add(appt);
			}
		} catch (Exception ee) {
			Errmsg.errmsg(ee);
		}

		return (av);

	}

	// return true if there are any todos
	public boolean haveTodos() {
		try {
			AppointmentDB kf = (AppointmentDB) db_;
			Collection<Integer> keycol = kf.getTodoKeys();
			// Collection keycol = AppointmentHelper.getTodoKeys(db_);
			if (keycol.size() != 0)
				return (true);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		return (false);
	}

	public Collection<String> getCategories() {

		TreeSet<String> dbcat = new TreeSet<String>();
		dbcat.add(CategoryModel.UNCATEGORIZED);
		try {
			for (Appointment ap : getAllAppts()) {
				String cat = ap.getCategory();
				if (cat != null && !cat.equals(""))
					dbcat.add(cat);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		return (dbcat);

	}

	// the calmodel keeps a hashmap of days to appt keys to avoid hitting
	// the DB when possible - although the DB is cached too to some extent
	// buildmap will rebuild the map based on the DB
	private void buildMap() throws Exception {
		// erase the current map
		map_.clear();
		vacationMap_.clear();

		// get the year for later
		GregorianCalendar cal = new GregorianCalendar();
		int curyr = cal.get(Calendar.YEAR);

		// scan entire DB
		AppointmentDB kf = (AppointmentDB) db_;
		Collection<Integer> rptkeys = kf.getRepeatKeys();

		for (Appointment appt : getAllAppts()) {

			if (!CategoryModel.getReference().isShown(appt.getCategory())) {
				continue;
			}

			// if appt does not repeat, we can add its
			// key to a single day
			int key = appt.getKey();
			Integer ki = new Integer(key);
			if (!rptkeys.contains(ki)) {
				// strip of appt number
				int dkey = (key / 100) * 100;

				// get/add entry for the day in the map
				Collection<Integer> o = map_.get(new Integer(dkey));
				if (o == null) {
					o = new LinkedList<Integer>();
					map_.put(new Integer(dkey), o);
				}

				// add the appt key to the day's list
				LinkedList<Integer> l = (LinkedList<Integer>) o;
				l.add(new Integer(key));

				// add day key to vacation map if appt has vacation
				if (appt.getVacation() != null
						&& appt.getVacation().intValue() != 0) {
					vacationMap_.put(new Integer(dkey), appt.getVacation()
							.intValue());
				}
			} else {
				// appt repeats so we have to add all of the repeats
				// into the map (well maybe not all)
				int yr = (key / 1000000) % 1000 + 1900;
				int mo = (key / 10000) % 100 - 1;
				int day = (key / 100) % 100;
				cal.set(yr, mo, day);

				Repeat repeat = new Repeat(cal, appt.getFrequency());
				if (!repeat.isRepeating())
					continue;
				Integer times = appt.getTimes();
				if (times == null)
					times = new Integer(1);
				int tm = times.intValue();

				// ok, plod through the repeats now
				for (int i = 0; i < tm; i++) {
					Calendar current = repeat.current();
					if (current == null) {
						repeat.next();
						continue;
					}

					// get the day key for the repeat
					int rkey = dkey(current);

					int cyear = current.get(Calendar.YEAR);

					// limit the repeats to 2 years
					// from the current year
					// otherwise, an appt repeating 9999 times
					// could kill BORG
					if (cyear > curyr + 2)
						break;

					// check if the repeat is in the skip list
					// if so, skip it
					if (!isSkipped(appt, current)) {
						// add the repeat key to the map
						Collection<Integer> o = map_.get(new Integer(rkey));
						if (o == null) {
							o = new LinkedList<Integer>();
							map_.put(new Integer(rkey), o);
						}
						LinkedList<Integer> l = (LinkedList<Integer>) o;
						l.add(new Integer(key));

						// add day key to vacation map if appt has vacation
						if (appt.getVacation() != null
								&& appt.getVacation().intValue() != 0) {
							vacationMap_.put(new Integer(rkey), appt
									.getVacation());
						}
					}

					repeat.next();
				}
			}
		}

	}

	// determine the number of vacation days up to and including the given day
	// for the current year
	public double vacationCount(int dkey) {
		// System.out.println("dkey=" + dkey);
		int yr = (dkey / 1000000) % 1000 + 1900;
		int yearStartKey = dkey(yr, 1, 1);
		double count = 0;

		Set<Integer> vkeys = vacationMap_.keySet();
		for (Integer i : vkeys) {
			int vdaykey = i.intValue();
			if (vdaykey >= yearStartKey && vdaykey <= dkey) {
				Integer vnum = vacationMap_.get(i);
				if (vnum.intValue() == 2) {
					count += 0.5;
				} else {
					count += 1.0;
				}
			}
		}

		return count;

	}

	// export the appt data to a file in XML
	public void export(Writer fw) throws Exception {

		// FileWriter fw = new FileWriter(fname);
		fw.write("<APPTS>\n");
		AppointmentXMLAdapter aa = new AppointmentXMLAdapter();

		// export appts
		for (Appointment ap : getAllAppts()) {
			XTree xt = aa.toXml(ap);
			fw.write(xt.toString());
		}

		fw.write("</APPTS>");

	}

	// export the appt data to a file in XML
	public void importXml(XTree xt) throws Exception {

		AppointmentXMLAdapter aa = new AppointmentXMLAdapter();

		// for each appt - create an Appointment and store
		for (int i = 1;; i++) {
			XTree ch = xt.child(i);
			if (ch == null)
				break;

			if (!ch.name().equals("Appointment"))
				continue;
			Appointment appt = aa.fromXml(ch);

			// create new key if none exists
			if (appt.getKey() == 0) {
				Date d = appt.getDate();
				GregorianCalendar gcal = new GregorianCalendar();
				gcal.setTime(d);
				int key = AppointmentModel.dkey(gcal);
				appt.setKey(key);
			}

			while (true) {
				try {
					db_.addObj(appt);
					break;

				} catch (Exception e) {
					Errmsg.errmsg(e);
					break;
				}
			}
		}

		// rebuild the hashmap
		buildMap();

		CategoryModel.getReference().syncCategories();

		// refresh all views that are displaying appt data from this model
		refreshListeners();

	}

	public static boolean isNote(Appointment appt) {
		// return true if the appt Appointment represents a "note" or
		// "non-timed" appt
		// this is true if the time is midnight and duration is 0.
		// this method was used for backward compatibility - as opposed to
		// adding
		// a new flag to the DB
		// 1.6.1 - added new db field to fix bug when time zone changes
		// for backward compatiblity, keep old check in addition to checking new
		// flag
		try {

			if (appt.getUntimed() != null && appt.getUntimed().equals("Y"))
				return true;
			Integer duration = appt.getDuration();
			if (duration != null && duration.intValue() != 0)
				return (false);

			Date d = appt.getDate();
			if (d == null)
				return (true);

			GregorianCalendar g = new GregorianCalendar();
			g.setTime(d);
			int hour = g.get(Calendar.HOUR_OF_DAY);
			if (hour != 0)
				return (false);

			int min = g.get(Calendar.MINUTE);
			if (min != 0)
				return (false);
		} catch (Exception e) {
			return (true);
		}

		return (true);

	}

	public static SimpleDateFormat getTimeFormat() {
		String mt = Prefs.getPref(PrefName.MILTIME);
		if (mt.equals("true")) {
			return (new SimpleDateFormat("HH:mm"));
		}

		return (new SimpleDateFormat("h:mm a"));

	}

	public void sync() {
		db_.sync();
		try {
			// recreate the appointment hashmap
			buildMap();

			// refresh all views that are displaying appt data from this
			// model
			refreshListeners();
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}
	}

	public Collection<Appointment> getAllAppts() throws Exception {
		Collection<Appointment> appts = db_.readAll();
		return appts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	public void refresh() {

		try {
			buildMap();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// refresh all views that are displaying appt data from this model
		refreshListeners();

	}

}
