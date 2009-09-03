/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui.calendar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.XTree;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.LabelEntity;
import net.sf.borg.model.xml.AppointmentXMLAdapter;

abstract class ApptBoxPanel extends JPanel {

	private class ClickedBoxInfo {
		public Box box = null;
		public boolean onBottomBorder = false;
		public boolean onTopBorder = false;
		public DateZone zone = null;
	}

	private class DragNewBox extends Box {

		private static final int radius = 5;

		JPopupMenu pop = null;
		public DragNewBox(Rectangle bounds, Rectangle clip) {
			super(bounds, clip);
		}

		private void addAppt() {
			String text = JOptionPane.showInputDialog("", Resource
					.getResourceString("Please_enter_some_appointment_text"));
			if (text == null)
				return;
			Rectangle r = getBounds();
			int topmins = realMins((r.y - resizeYMin)
					/ (resizeYMax - resizeYMin));
			int botmins = realMins((r.y - resizeYMin + r.height)
					/ (resizeYMax - resizeYMin));
			
			// get default appt values, if any
			Appointment appt = null;
			String defApptXml = Prefs.getPref(PrefName.DEFAULT_APPT);
			if (!defApptXml.equals("")) {
				try {
					XTree xt = XTree.readFromBuffer(defApptXml);
					AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
					appt = axa.fromXml(xt);

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}

			if (appt == null) {
				appt = AppointmentModel.getReference().newAppt();
			}

			// System.out.println(top + " " + bottom);
			int realtime = topmins;
			int hour = realtime / 60;
			int min = realtime % 60;
			min = (min / 5) * 5;
			Calendar startCal = new GregorianCalendar();
			startCal.setTime(draggedZone.getDate());
			startCal.set(Calendar.HOUR_OF_DAY, hour);
			startCal.set(Calendar.MINUTE, min);
			appt.setDate(startCal.getTime());

			int realend = botmins;
			int ehour = realend / 60;
			int emin = realend % 60;
			emin = (emin / 5) * 5;
			int dur = 60 * (ehour - hour) + emin - min;

			appt.setDuration(new Integer(dur));

			if (dur > 0)
				appt.setUntimed(null);

			appt.setText(text);
			AppointmentModel.getReference().saveAppt(appt);
			
			draggedZone = null;
			removeDragNewBox();
			repaint();
		}
		

		public void draw(Graphics2D g2, Component comp) {
			Stroke stroke = g2.getStroke();
			g2.setStroke(thicker);
			g2.setColor(Color.GREEN);
			if (isSelected == true) {
				g2.setColor(Color.CYAN);
			}
			Rectangle r = getBounds();
			g2.drawRoundRect(r.x, r.y, r.width, r.height, radius * radius,
					radius * radius);
			g2.setStroke(stroke);
			double top = (r.y - resizeYMin) / (resizeYMax - resizeYMin);
			double bot = (r.y - resizeYMin + r.height)
					/ (resizeYMax - resizeYMin);
			g2.setColor(new Color(50, 50, 50));
			Rectangle2D bb = g2.getFont().getStringBounds("00:00",
					g2.getFontRenderContext());
			g2.fillRect(r.x + 2, r.y - (int) bb.getHeight(), (int) bb
					.getWidth(), (int) bb.getHeight());
			g2.fillRect(r.x + 2, r.y + r.height - (int) bb.getHeight(),
					(int) bb.getWidth(), (int) bb.getHeight());
			g2.setColor(Color.WHITE);
			g2.drawString(getTimeString(top), r.x + 2, r.y - 2);
			g2.drawString(getTimeString(bot), r.x + 2, r.y + r.height - 2);

		}

		public void onClick() {
			addAppt();
		}

		public JPopupMenu getMenu() {
			if (pop == null) {
				JMenuItem mnuitm = null;
				pop = new JPopupMenu();
				pop.add(mnuitm = new JMenuItem(Resource
						.getResourceString("Add_New")));
				mnuitm.addActionListener(new ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						addAppt();
					}
				});
			}
			return pop;
		}

	}

	private class MyComponentListener implements ComponentListener {

		public void componentHidden(ComponentEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

		public void componentResized(ComponentEvent e) {
			ApptBoxPanel p = (ApptBoxPanel) e.getComponent();
			p.refresh();
		}

		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

	}

	private class MyMouseListener implements MouseListener, MouseMotionListener {

		private boolean resizeTop = true;

		public MyMouseListener() {

		}

		public void mouseClicked(MouseEvent evt) {

			evt.translatePoint(translation, translation);
			ClickedBoxInfo b = getClickedBoxInfo(evt);
			if (evt.getButton() == MouseEvent.BUTTON3) {

				if (b == null)
					return;

				if (b.box != null) {
					if (b.box.getMenu() != null) {
						b.box.getMenu().show(evt.getComponent(), evt.getX(),
								evt.getY());
					}
				} else if (b.zone != null) {
					b.zone.getMenu().show(evt.getComponent(), evt.getX(),
							evt.getY());
				}

				return;
			}

			evt.getComponent().repaint();

			if (b == null)
				return;
			else if (b.box != null)
				b.box.onClick();
			else if (b.zone != null)
				b.zone.onClick();

		}

		public void mouseDragged(MouseEvent evt) {
			evt.translatePoint(translation, translation);

			if (evt.getButton() == MouseEvent.BUTTON3)
				return;

			dragStarted = true;
			if (resizedBox != null) {
				if (resizeTop == true) {
					int top = (int) Math.max(evt.getY(), resizeYMin);
					setResizeBox(resizedBox.getBounds().x, top, resizedBox
							.getBounds().width, resizedBox.getBounds().height
							+ resizedBox.getBounds().y - top);
				} else {
					int bot = (int) Math.min(evt.getY(), resizeYMax);
					setResizeBox(resizedBox.getBounds().x, resizedBox
							.getBounds().y, resizedBox.getBounds().width, bot
							- resizedBox.getBounds().y);
				}
				evt.getComponent().repaint();
			} else if (draggedBox != null) {

				int top = evt.getY() - (draggedBox.getBounds().height / 2);
				if (top < dragYMin)
					top = (int) dragYMin;
				if (top + draggedBox.getBounds().height > dragYMax)
					top = (int) dragYMax - draggedBox.getBounds().height;
				int left = evt.getX() - (draggedBox.getBounds().width / 2);
				if (left < dragXMin)
					left = (int) dragXMin;
				if (left + draggedBox.getBounds().width > dragXMax)
					left = (int) dragXMax - draggedBox.getBounds().width;
				setResizeBox(left, top, draggedBox.getBounds().width,
						draggedBox.getBounds().height);

				evt.getComponent().repaint();
			} else if (draggedAnchor != -1) {
				if (dragNewBox == null)
					setDragNewBox(draggedZone.getBounds().x, evt.getY(),
							draggedZone.getBounds().width, 5);
				double y = evt.getY();
				y = Math.max(y, resizeYMin);
				y = Math.min(y, resizeYMax);
				Rectangle r = dragNewBox.getBounds();
				if (y > draggedAnchor) {
					setDragNewBox(r.x, r.y, r.width, y - draggedAnchor);
				} else {
					setDragNewBox(r.x, y, r.width, draggedAnchor - y);
				}
				evt.getComponent().repaint();
			}
		}

		public void mouseEntered(MouseEvent evt) {
		}

		public void mouseExited(MouseEvent evt) {
		}

		public void mouseMoved(MouseEvent evt) {
			evt.translatePoint(translation, translation);

			if (evt.getButton() == MouseEvent.BUTTON3)
				return;

			JPanel panel = (JPanel) evt.getComponent();

			ClickedBoxInfo b = getClickedBoxInfo(evt);
			if (b != null && b.box != null) {
				panel.setToolTipText(b.box.getText());
			}

			if (b != null && (b.onTopBorder || b.onBottomBorder)) {
				panel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
			} else if (b != null && b.box != null && b.box instanceof Box.Draggable) {
				panel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			} else {
				panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			evt.getComponent().repaint();
		}

		public void mousePressed(MouseEvent evt) {

			evt.translatePoint(translation, translation);

			if (evt.getButton() == MouseEvent.BUTTON3)
				return;

			ClickedBoxInfo b = getClickedBoxInfo(evt);
			if (b == null || b.box != dragNewBox)
				removeDragNewBox();

			dragStarted = false;

			if (b != null && (b.onTopBorder || b.onBottomBorder)) {
				if (b.box instanceof ApptBox) {
					resizedBox = (ApptBox) b.box;
					setResizeBox(b.box.getBounds().x, b.box.getBounds().y,
							b.box.getBounds().width, b.box.getBounds().height);
					if (b.onBottomBorder) {
						resizeTop = false;
					} else {
						resizeTop = true;
					}
				} else if (b.box == dragNewBox) {
					draggedZone = b.zone;
					if (b.onBottomBorder) {
						draggedAnchor = dragNewBox.getBounds().y;
					} else {
						draggedAnchor = dragNewBox.getBounds().y
								+ dragNewBox.getBounds().height;
					}
				}
				evt.getComponent().repaint();
			} else if (b != null && b.box != null && b.box instanceof Box.Draggable) {
				draggedBox = b.box;
				setResizeBox(b.box.getBounds().x, b.box.getBounds().y, b.box
						.getBounds().width, b.box.getBounds().height);
				evt.getComponent().repaint();
			} else if (b != null && b.zone != null && evt.getY() > resizeYMin
					&& evt.getY() < resizeYMax) {
				// b is a date zone
				draggedZone = b.zone;
				draggedAnchor = evt.getY();
			}

			JPanel panel = (JPanel) evt.getComponent();
			if (b != null && (b.onTopBorder || b.onBottomBorder)) {
				panel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
			} else if (b != null && b.box != null && b.box instanceof Box.Draggable) {
				panel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			} else {
				panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		}

		public void mouseReleased(MouseEvent evt) {
			evt.translatePoint(translation, translation);

			if (evt.getButton() == MouseEvent.BUTTON3)
				return;
			if (resizedBox != null && dragStarted) {
				double y = evt.getY();
				y = Math.max(y, resizeYMin);
				y = Math.min(y, resizeYMax);
				try {
					resizedBox.resize(resizeTop, realMins((y - resizeYMin)
							/ (resizeYMax - resizeYMin)));
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

			} else if (draggedBox != null && dragStarted) {
				double y = resizeRectangle.y;
				y = Math.max(y, resizeYMin);
				y = Math.min(y, resizeYMax);

				double centerx = evt.getX();// + draggedBox.getBounds().width /
				// 2;
				double centery = evt.getY();// + draggedBox.getBounds().height /
				// 2;
				Date d = getDateForCoord(centerx, centery);
				try {
					if (isInsideResizeArea(resizeRectangle.y, resizeRectangle.y
							+ resizeRectangle.height)) {
						((Box.Draggable)draggedBox).move(realMins((y - resizeYMin)
								/ (resizeYMax - resizeYMin)), d);
					} else {
						((Box.Draggable)draggedBox).move(0, d);
					}
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

			}

			draggedBox = null;
			resizedBox = null;
			removeResizeBox();
			evt.getComponent().repaint();

		}
	}
	
	final static private float hlthickness = 2.0f;

	final static private BasicStroke highlight = new BasicStroke(hlthickness);


	private static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");

	// final static private BasicStroke regular = new BasicStroke(1.0f);
	final static private BasicStroke thicker = new BasicStroke(4.0f);

	// Graphics2D is translated

	final static private int translation = -10; // to adjust, since since

	public static boolean isStrike(CalendarEntity appt, Date date) {
		if ((appt.getColor() != null && appt.getColor().equals("strike"))
				|| (appt.getTodo() && !(appt.getNextTodo() == null || !appt
						.getNextTodo().after(date)))) {
			return (true);
		}
		return false;
	}

	protected Collection<Object> boxes = new ArrayList<Object>();

	private int boxnum = 0;

	private int draggedAnchor = -1;

	private Box draggedBox = null;

	private DateZone draggedZone = null;

	private DragNewBox dragNewBox = null;

	private boolean dragStarted = false;

	private double dragXMax = 0;;

	private double dragXMin = 0;

	private double dragYMax = 0;;

	private double dragYMin = 0;

	protected double endmin = 0;;

	private ApptBox resizedBox = null;

	private Rectangle resizeRectangle = null;

	private double resizeYMax = 0;

	private double resizeYMin = 0;

	protected double startmin = 0;

	private Collection<Object> zones = new ArrayList<Object>();

	public ApptBoxPanel() {

		MyMouseListener myOneListener = new MyMouseListener();
		addMouseListener(myOneListener);
		addMouseMotionListener(myOneListener);
		addComponentListener(new MyComponentListener());

	}

	public void addApptBox(Date d, Appointment ap, Rectangle bounds,
			Rectangle clip) {

		if (Prefs.getBoolPref(PrefName.HIDESTRIKETHROUGH)
				&& ApptBoxPanel.isStrike(ap, d))
			return;
		ApptBox b = new ApptBox(d, ap, bounds, clip);

		b.setBoxnum(boxnum);
		boxnum++;

		boxes.add(b);
	}

	public void addDateZone(Date d, double sm, double em, Rectangle bounds) {
		DateZone b = new DateZone(d, bounds);
		zones.add(b);
	}

	public Box addNoteBox(Date d, CalendarEntity ap, Rectangle bounds,
			Rectangle clip) {

		if (Prefs.getBoolPref(PrefName.HIDESTRIKETHROUGH)
				&& ApptBoxPanel.isStrike(ap, d))
			return null;

		Box b;
		if (ap instanceof LabelEntity) {
			// phony holiday appt added by Day object
			b = new LabelBox((LabelEntity)ap, bounds, clip);
		} else {
			b = new NoteBox(d, ap, bounds, clip);
		}
		boxes.add(b);

		return b;
	}

	public void clearBoxes() {
		boxes.clear();
		zones.clear();
		boxnum = 0;
	}

	public void drawBoxes(Graphics2D g2) {

		Stroke stroke = g2.getStroke();

		Iterator<Object> it = boxes.iterator();
		while (it.hasNext()) {
			Box b = (Box) it.next();
			b.draw(g2, this);
		}

		int radius = 5;
		if (resizeRectangle != null) {
			g2.setStroke(highlight);
			g2.setColor(Color.RED);
			g2.drawRoundRect(resizeRectangle.x, resizeRectangle.y,
					resizeRectangle.width, resizeRectangle.height, radius
							* radius, radius * radius);
			g2.setStroke(stroke);

			if (isInsideResizeArea(resizeRectangle.y, resizeRectangle.y
					+ resizeRectangle.height)) {
				double top = (resizeRectangle.y - resizeYMin)
						/ (resizeYMax - resizeYMin);
				double bot = (resizeRectangle.y - resizeYMin + resizeRectangle.height)
						/ (resizeYMax - resizeYMin);
				g2.setColor(new Color(50, 50, 50));
				Rectangle2D bb = g2.getFont().getStringBounds("00:00",
						g2.getFontRenderContext());
				g2.fillRect(resizeRectangle.x + 2, resizeRectangle.y
						- (int) bb.getHeight(), (int) bb.getWidth(), (int) bb
						.getHeight());
				g2.fillRect(resizeRectangle.x + 2, resizeRectangle.y
						+ resizeRectangle.height - (int) bb.getHeight(),
						(int) bb.getWidth(), (int) bb.getHeight());
				g2.setColor(Color.WHITE);
				g2.drawString(getTimeString(top), resizeRectangle.x + 2,
						resizeRectangle.y - 2);
				g2.drawString(getTimeString(bot), resizeRectangle.x + 2,
						resizeRectangle.y + resizeRectangle.height - 2);
			}
		}
		if (dragNewBox != null) {
			dragNewBox.draw(g2, this);
		}
		g2.setColor(Color.black);

	}

	private ClickedBoxInfo getClickedBoxInfo(MouseEvent evt) {
		// determine which box is selected, if any

		boolean onTopBorder = false;
		boolean onBottomBorder = false;
		ClickedBoxInfo ret = new ClickedBoxInfo();

		if (dragNewBox != null) {
			Rectangle r = dragNewBox.getBounds();
			if (evt.getX() > r.x && evt.getX() < (r.x + r.width)
					&& evt.getY() > r.y && evt.getY() < (r.y + r.height)) {
				ret.box = dragNewBox;
				dragNewBox.setSelected(true);

				if (Math.abs(evt.getY() - r.y) < 4) {
					onTopBorder = true;
				} else if (Math.abs(evt.getY() - (r.y + r.height)) < 4) {
					onBottomBorder = true;
				}

			} else {
				dragNewBox.setSelected(false);
			}
		}

		Iterator<Object> it = boxes.iterator();
		while (it.hasNext()) {

			Box b = (Box) it.next();

			if (ret.box == null && evt.getX() > b.getBounds().x
					&& evt.getX() < (b.getBounds().x + b.getBounds().width)
					&& evt.getY() > b.getBounds().y
					&& evt.getY() < (b.getBounds().y + b.getBounds().height)) {

				b.setSelected(true);
				ret.box = b;
				if (b instanceof ApptBox) {
					if (Math.abs(evt.getY() - b.getBounds().y) < 4) {
						onTopBorder = true;
					} else if (Math.abs(evt.getY()
							- (b.getBounds().y + b.getBounds().height)) < 4) {
						onBottomBorder = true;
					}
				}
			} else {
				b.setSelected(false);
			}
		}

		it = zones.iterator();
		while (it.hasNext()) {

			DateZone b = (DateZone) it.next();
			if (evt.getX() > b.getBounds().x
					&& evt.getX() < (b.getBounds().x + b.getBounds().width)
					&& evt.getY() > b.getBounds().y
					&& evt.getY() < (b.getBounds().y + b.getBounds().height)) {
				ret.zone = b;
				break;
			}
		}

		ret.onTopBorder = onTopBorder;
		ret.onBottomBorder = onBottomBorder;

		return ret;

	}

	abstract Date getDateForCoord(double x, double y);

	public String getTimeString(double y_fraction) {

		int realtime = realMins(y_fraction);
		int hour = realtime / 60;
		int min = realtime % 60;
		GregorianCalendar newCal = new GregorianCalendar();
		newCal.set(Calendar.HOUR_OF_DAY, hour);
		int roundMin = (min / 5) * 5;
		newCal.set(Calendar.MINUTE, roundMin);
		Date newTime = newCal.getTime();
		return sdf.format(newTime);
	}

	private boolean isInsideResizeArea(int top, int bot) {
		if (top >= resizeYMin && bot <= resizeYMax)
			return true;
		return false;

	}

	private int realMins(double y_fraction) {
		double realtime = startmin + (endmin - startmin) * y_fraction;
		// round it because the double math is causing errors when later
		// converting to int
		int min = 5 * (int) Math.round(realtime / 5);
		return min;
	}

	public abstract void refresh();

	public void removeDragNewBox() {
		dragNewBox = null;
		draggedAnchor = -1;
	}

	public void removeResizeBox() {
		resizeRectangle = null;
	}

	public void setDragBounds(int ymin, int ymax, int xmin, int xmax) {
		dragYMin = ymin;
		dragYMax = ymax;
		dragXMin = xmin;
		dragXMax = xmax;
	}

	public void setDragNewBox(double x, double y, double w, double h) {

		Rectangle bounds = new Rectangle();
		bounds.x = (int) x;
		bounds.y = (int) y;
		bounds.height = (int) h;
		bounds.width = (int) w;
		if (dragNewBox == null) {		
			dragNewBox = new DragNewBox(bounds, bounds);
		}
		dragNewBox.setBounds(bounds);

	}

	public void setResizeBounds(int ymin, int ymax, int xmin, int xmax) {
		resizeYMin = ymin;
		resizeYMax = ymax;
		// resizeXMin = xmin;
		// resizeXMax = xmax;
	}

	public void setResizeBox(double x, double y, double w, double h) {
		if (resizeRectangle == null)
			resizeRectangle = new Rectangle();
		resizeRectangle.x = (int) x;
		resizeRectangle.y = (int) y;
		resizeRectangle.height = (int) h;
		resizeRectangle.width = (int) w;

	}

}
