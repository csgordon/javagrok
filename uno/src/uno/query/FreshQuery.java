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
import java.util.Set;

import soot.*;
import uno.CallGraph;

public class FreshQuery extends Query {

	private int index;
	private SootMethod method;
	boolean needed;

	public FreshQuery(SootMethod method, int index,boolean needed) {
		// TODO Auto-generated constructor stub
		this.method = method;
		this.index = index;
		this.needed = needed;
		ID = 3;
	}

	public SootMethod getMethod(){return method;}
	public boolean getStrict(){ return needed;}
	public boolean alwaysTrue(){
		if(method.getParameterType(index) instanceof PrimType) return true;
		if(method.getParameterType(index) instanceof RefType &&
				((RefType)method.getParameterType(index)).getClassName().equals("java.lang.String"))
			return true;
		return false;
	}

	protected List[] getQueries() {
		// TODO Auto-generated method stub
		Set callers = CallGraph.getCallersOf(method);
		Set superthisCallers = CallGraph.getSuperthisCallersOf(method);

		List[] queries = createListArray(3);

		for(Iterator i=uno.Hierarchy.getSuperMethodOf(method).iterator();i.hasNext();){
			SootMethod superm = (SootMethod) i.next();
			queries[0].add(QueryFactory.getFreshQuery(superm,index,needed));
		}
		for(Iterator i=callers.iterator();i.hasNext();){
			SootMethod caller = (SootMethod) i.next();
			queries[1].add(QueryFactory.getFreshInQuery(method,index,caller));
		}		
		for(Iterator i=superthisCallers.iterator();i.hasNext();){
			SootMethod caller = (SootMethod) i.next();
			queries[2].add(QueryFactory.getFreshInQuery(method,index,caller,needed));
		}		

		return queries;
	}

	public boolean thisLocalIsFalse(int rule){
		return false;
	}

	protected boolean isLocallyFalse() {
		// TODO Auto-generated method stub
		return false;
	}

	protected void setName() {
		// TODO Auto-generated method stub
		name = "UniqPar ( " + simplify(method) + " , " + (index) + " , "  + (needed) + " )";
	}

}
