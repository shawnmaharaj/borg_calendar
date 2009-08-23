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
package net.sf.borg.model.entity;



public class Address extends KeyedEntity<Address> implements java.io.Serializable {

	
	private static final long serialVersionUID = 1996612351860988688L;
	private String FirstName_;
	public String getFirstName() { return( FirstName_ ); }
	public void setFirstName( String xx ){ FirstName_ = xx; }

	private String LastName_;
	public String getLastName() { return( LastName_ ); }
	public void setLastName( String xx ){ LastName_ = xx; }

	private String Nickname_;
	public String getNickname() { return( Nickname_ ); }
	public void setNickname( String xx ){ Nickname_ = xx; }

	private String Email_;
	public String getEmail() { return( Email_ ); }
	public void setEmail( String xx ){ Email_ = xx; }

	private String ScreenName_;
	public String getScreenName() { return( ScreenName_ ); }
	public void setScreenName( String xx ){ ScreenName_ = xx; }

	private String WorkPhone_;
	public String getWorkPhone() { return( WorkPhone_ ); }
	public void setWorkPhone( String xx ){ WorkPhone_ = xx; }

	private String HomePhone_;
	public String getHomePhone() { return( HomePhone_ ); }
	public void setHomePhone( String xx ){ HomePhone_ = xx; }

	private String Fax_;
	public String getFax() { return( Fax_ ); }
	public void setFax( String xx ){ Fax_ = xx; }

	private String Pager_;
	public String getPager() { return( Pager_ ); }
	public void setPager( String xx ){ Pager_ = xx; }

	private String StreetAddress_;
	public String getStreetAddress() { return( StreetAddress_ ); }
	public void setStreetAddress( String xx ){ StreetAddress_ = xx; }

	private String City_;
	public String getCity() { return( City_ ); }
	public void setCity( String xx ){ City_ = xx; }

	private String State_;
	public String getState() { return( State_ ); }
	public void setState( String xx ){ State_ = xx; }

	private String Zip_;
	public String getZip() { return( Zip_ ); }
	public void setZip( String xx ){ Zip_ = xx; }

	private String Country_;
	public String getCountry() { return( Country_ ); }
	public void setCountry( String xx ){ Country_ = xx; }

	private String Company_;
	public String getCompany() { return( Company_ ); }
	public void setCompany( String xx ){ Company_ = xx; }

	private String WorkStreetAddress_;
	public String getWorkStreetAddress() { return( WorkStreetAddress_ ); }
	public void setWorkStreetAddress( String xx ){ WorkStreetAddress_ = xx; }

	private String WorkCity_;
	public String getWorkCity() { return( WorkCity_ ); }
	public void setWorkCity( String xx ){ WorkCity_ = xx; }

	private String WorkState_;
	public String getWorkState() { return( WorkState_ ); }
	public void setWorkState( String xx ){ WorkState_ = xx; }

	private String WorkZip_;
	public String getWorkZip() { return( WorkZip_ ); }
	public void setWorkZip( String xx ){ WorkZip_ = xx; }

	private String WorkCountry_;
	public String getWorkCountry() { return( WorkCountry_ ); }
	public void setWorkCountry( String xx ){ WorkCountry_ = xx; }

	private String WebPage_;
	public String getWebPage() { return( WebPage_ ); }
	public void setWebPage( String xx ){ WebPage_ = xx; }

	private String Notes_;
	public String getNotes() { return( Notes_ ); }
	public void setNotes( String xx ){ Notes_ = xx; }

	private java.util.Date Birthday_;
	public java.util.Date getBirthday() { return( Birthday_ ); }
	public void setBirthday( java.util.Date xx ){ Birthday_ = xx; }

	private boolean New_;
	public boolean getNew() { return( New_ ); }
	public void setNew( boolean xx ){ New_ = xx; }

	private boolean Modified_;
	public boolean getModified() { return( Modified_ ); }
	public void setModified( boolean xx ){ Modified_ = xx; }

	private boolean Deleted_;
	public boolean getDeleted() { return( Deleted_ ); }
	public void setDeleted( boolean xx ){ Deleted_ = xx; }

	protected Address clone() {
		Address dst = new Address();
		dst.setKey( getKey());
		dst.setFirstName( getFirstName() );
		dst.setLastName( getLastName() );
		dst.setNickname( getNickname() );
		dst.setEmail( getEmail() );
		dst.setScreenName( getScreenName() );
		dst.setWorkPhone( getWorkPhone() );
		dst.setHomePhone( getHomePhone() );
		dst.setFax( getFax() );
		dst.setPager( getPager() );
		dst.setStreetAddress( getStreetAddress() );
		dst.setCity( getCity() );
		dst.setState( getState() );
		dst.setZip( getZip() );
		dst.setCountry( getCountry() );
		dst.setCompany( getCompany() );
		dst.setWorkStreetAddress( getWorkStreetAddress() );
		dst.setWorkCity( getWorkCity() );
		dst.setWorkState( getWorkState() );
		dst.setWorkZip( getWorkZip() );
		dst.setWorkCountry( getWorkCountry() );
		dst.setWebPage( getWebPage() );
		dst.setNotes( getNotes() );
		dst.setBirthday( getBirthday() );
		dst.setNew( getNew() );
		dst.setModified( getModified() );
		dst.setDeleted( getDeleted() );
		return(dst);
	}
}