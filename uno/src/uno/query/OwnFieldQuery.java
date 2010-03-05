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

public class OwnFieldQuery extends Query {

	private SootField f;

	public OwnFieldQuery(SootField f) {
		// TODO Auto-generated constructor stub
		this.f = f;
		ID = 11;
	}

	public SootField getField(){ return f;}

	protected List[] getQueries() {
		// TODO Auto-generated method stub

		List[] r = new List[2];
		r[0] = new LinkedList();
		r[0].add(QueryFactory.getFFieldQuery(f));

		List queries = new LinkedList();
		for(Iterator i=f.getDeclaringClass().getMethods().iterator();i.hasNext();){
			queries.add(QueryFactory.getOwnFieldInQuery(f,(SootMethod)i.next()));
		}
		r[1] = queries;
		return r;
	}

	public boolean alwaysTrue(){
		return false;
	}

	public boolean thisLocalIsFalse(int rule){
		return false;
	}
	protected boolean isLocallyFalse() {
		return false;
	}

	protected void setName() {
		// TODO Auto-generated method stub
		name = "OwnField ( " + simplify(f) + " )";
	}

}
