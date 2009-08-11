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
package net.sf.borg.model.beans;

import net.sf.borg.common.XTree;
public class ProjectXMLAdapter extends BeanXMLAdapter<Project> {

	public XTree toXml( Project o )
	{
		
		XTree xt = new XTree();
		xt.name("Project");
		xt.appendChild("KEY", Integer.toString(o.getKey()));
		if( o.getId() != null )
			xt.appendChild("Id", BeanXMLAdapter.toString(o.getId()));
		if( o.getStartDate() != null )
			xt.appendChild("StartDate", BeanXMLAdapter.toString(o.getStartDate()));
		if( o.getDueDate() != null )
			xt.appendChild("DueDate", BeanXMLAdapter.toString(o.getDueDate()));
		if( o.getDescription() != null && !o.getDescription().equals(""))
			xt.appendChild("Description", o.getDescription());
		if( o.getCategory() != null && !o.getCategory().equals(""))
			xt.appendChild("Category", o.getCategory());
		if( o.getStatus() != null && !o.getStatus().equals(""))
			xt.appendChild("Status", o.getStatus());
		if( o.getParent() != null )
			xt.appendChild("Parent", BeanXMLAdapter.toString(o.getParent()));
		return( xt );
	}

	public Project fromXml( XTree xt )
	{
		Project ret = new Project();
		String ks = xt.child("KEY").value();
		ret.setKey( BeanXMLAdapter.toInt(ks) );
		String val = "";
		val = xt.child("Id").value();
		ret.setId( BeanXMLAdapter.toInteger(val) );
		val = xt.child("StartDate").value();
		ret.setStartDate( BeanXMLAdapter.toDate(val) );
		val = xt.child("DueDate").value();
		ret.setDueDate( BeanXMLAdapter.toDate(val) );
		val = xt.child("Description").value();
		if( !val.equals("") )
			ret.setDescription( val );
		val = xt.child("Category").value();
		if( !val.equals("") )
			ret.setCategory( val );
		val = xt.child("Status").value();
		if( !val.equals("") )
			ret.setStatus( val );
		val = xt.child("Parent").value();
		ret.setParent( BeanXMLAdapter.toInteger(val) );
		return( ret );
	}
}
