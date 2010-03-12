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

package uno.query;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.SootField;
import soot.SootMethod;
import uno.analysis.PointsToAnalysis;
import uno.analysis.PointsToAnalysisFactory;

public class FFieldQuery extends Query {

	private SootField f;

	public FFieldQuery(SootField f) {
		// TODO Auto-generated constructor stub
		this.f = f;
		ID = 7;
	}

	public SootField getField(){ return f;}
	protected List[] getQueries() {
		// TODO Auto-generated method stub
		List queries = new LinkedList();
		for(Iterator i=f.getDeclaringClass().getMethods().iterator();i.hasNext();){
			queries.add(QueryFactory.getFFieldInQuery(f,(SootMethod)i.next()));
		}
		List[] r = new List[1];
		r[0] = queries;
		return r;
	}

	public boolean alwaysTrue(){
		return false;
	}

	public boolean thisLocalIsFalse(int rule){
		if(rule==0)
		return !f.isPrivate();
		else return false;
	}
	protected boolean isLocallyFalse() {
		// TODO Auto-generated method stub
		return !f.isPrivate();
	}

	protected void setName() {
		// TODO Auto-generated method stub
		name = "NEscField ( " + simplify(f) + " )";
	}

}
