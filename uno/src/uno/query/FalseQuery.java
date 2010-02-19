///////////////////////////////////////////////////////////////////////
// Inferring Aliasing and Ecapsulation Properties for JAVA           //
// Copyright (C) 2007  Kin-Keung Ma, Jeffrey S. Foster               //
//                                                                   //
// This program is free software; you can redistribute it and/or     //
// modify it under the terms of the GNU General Public License       //
// as published by the Free Software Foundation; either version 2    //
// of the License, or (at your option) any later version.            //
//                                                                   //
// This program is distributed in the hope that it will be useful,   //
// but WITHOUT ANY WARRANTY; without even the implied warranty of    //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the     //
// GNU General Public License for more details.                      //
//                                                                   //
// You should have received a copy of the GNU General Public License //
// along with this program; if not, write to the Free Software       //
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA     //
// 02110-1301, USA.                                                  //
///////////////////////////////////////////////////////////////////////

/*
 * Created on Apr 8, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package uno.query;

import java.util.LinkedList;
import java.util.List;

import soot.jimple.InvokeExpr;
import uno.toolkit.Bool;

/**
 * @author kkma
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FalseQuery extends Query {
	private Query query;
	private InvokeExpr inv;
	
	public FalseQuery(Query query){
		this.query = query;
		query.setTruth(Bool.FALSE);
	}
	/**
	 * @param inv
	 */
	public FalseQuery(InvokeExpr inv) {
		this.inv = inv;
	}
	public boolean alwaysTrue(){return false;}
	/* (non-Javadoc)
	 * @see uno.query.Query#getQueries()
	 */
	protected List[] getQueries() {
		// TODO Auto-generated method stub
		List[] r = new List[1];
		r[0] = new LinkedList();
		return r;
	}

	/* (non-Javadoc)
	 * @see uno.query.Query#isLocallyFalse()
	 */
	public boolean thisLocalIsFalse(int rule){
		return true;
	}
	protected boolean isLocallyFalse() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see uno.query.Query#setName()
	 */
	protected void setName() {
		// TODO Auto-generated method stub
		if(query!=null) {
			query.setName();
			name = "(native) " + query.name;
		}
		else name = "(absent) " + inv;
	}

}
