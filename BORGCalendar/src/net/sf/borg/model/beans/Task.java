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

import java.util.Date;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;



public class Task extends KeyedBean<Task> implements CalendarBean, java.io.Serializable {

	
	private static final long serialVersionUID = -8980203293028263282L;
	private Integer TaskNumber_;
	public Integer getTaskNumber() { return( TaskNumber_ ); }
	public void setTaskNumber( Integer xx ){ TaskNumber_ = xx; }

	private java.util.Date StartDate_;
	public java.util.Date getStartDate() { return( StartDate_ ); }
	public void setStartDate( java.util.Date xx ){ StartDate_ = xx; }

	private java.util.Date CD_;
	public java.util.Date getCD() { return( CD_ ); }
	public void setCD( java.util.Date xx ){ CD_ = xx; }

	private java.util.Date DueDate_;
	public java.util.Date getDueDate() { return( DueDate_ ); }
	public void setDueDate( java.util.Date xx ){ DueDate_ = xx; }

	private java.util.Date ET_;
	public java.util.Date getET() { return( ET_ ); }
	public void setET( java.util.Date xx ){ ET_ = xx; }

	private String PersonAssigned_;
	public String getPersonAssigned() { return( PersonAssigned_ ); }
	public void setPersonAssigned( String xx ){ PersonAssigned_ = xx; }

	private Integer Priority_;
	public Integer getPriority() { return( Priority_ ); }
	public void setPriority( Integer xx ){ Priority_ = xx; }

	private String State_;
	public String getState() { return( State_ ); }
	public void setState( String xx ){ State_ = xx; }

	private String Type_;
	public String getType() { return( Type_ ); }
	public void setType( String xx ){ Type_ = xx; }

	private String Description_;
	public String getDescription() { return( Description_ ); }
	public void setDescription( String xx ){ Description_ = xx; }

	private String Resolution_;
	public String getResolution() { return( Resolution_ ); }
	public void setResolution( String xx ){ Resolution_ = xx; }

	private String TodoList_;
	public String getTodoList() { return( TodoList_ ); }
	public void setTodoList( String xx ){ TodoList_ = xx; }

	private String UserTask1_;
	public String getUserTask1() { return( UserTask1_ ); }
	public void setUserTask1( String xx ){ UserTask1_ = xx; }

	private String UserTask2_;
	public String getUserTask2() { return( UserTask2_ ); }
	public void setUserTask2( String xx ){ UserTask2_ = xx; }

	private String UserTask3_;
	public String getUserTask3() { return( UserTask3_ ); }
	public void setUserTask3( String xx ){ UserTask3_ = xx; }

	private String UserTask4_;
	public String getUserTask4() { return( UserTask4_ ); }
	public void setUserTask4( String xx ){ UserTask4_ = xx; }

	private String UserTask5_;
	public String getUserTask5() { return( UserTask5_ ); }
	public void setUserTask5( String xx ){ UserTask5_ = xx; }

	private String Category_;
	public String getCategory() { return( Category_ ); }
	public void setCategory( String xx ){ Category_ = xx; }

	private Integer Project_;
	public Integer getProject() { return( Project_ ); }
	public void setProject( Integer xx ){ Project_ = xx; }
	
	public String getColor()
	{
		return "navy";
	}
	
	public Integer getDuration()
	{
		return new Integer(0);
	}
	
	public Date getDate(){ return getDueDate(); }
	
	public boolean getTodo(){ return true; }
	
	public Date getNextTodo(){ return null; }
	
	public String getText(){
		 String show_abb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		 String abb = "";
         if (show_abb.equals("true"))
             abb = "BT" + getTaskNumber().toString() + " ";
         String de = abb + getDescription();
         String tx = de.replace('\n', ' ');

         return tx;
	}

	protected Task clone() {
		Task dst = new Task();
		dst.setKey( getKey());
		dst.setTaskNumber( getTaskNumber() );
		dst.setStartDate( getStartDate() );
		dst.setCD( getCD() );
		dst.setDueDate( getDueDate() );
		dst.setET( getET() );
		dst.setPersonAssigned( getPersonAssigned() );
		dst.setPriority( getPriority() );
		dst.setState( getState() );
		dst.setType( getType() );
		dst.setDescription( getDescription() );
		dst.setResolution( getResolution() );
		dst.setTodoList( getTodoList() );
		dst.setUserTask1( getUserTask1() );
		dst.setUserTask2( getUserTask2() );
		dst.setUserTask3( getUserTask3() );
		dst.setUserTask4( getUserTask4() );
		dst.setUserTask5( getUserTask5() );
		dst.setCategory( getCategory() );
		dst.setProject( getProject() );
		return(dst);
	}
}
