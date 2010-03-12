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
import uno.analysis.PointsToAnalysis;
import uno.analysis.PointsToAnalysisFactory;
import uno.toolkit.*;

public class OwnParQuery extends Query{
	
	private SootMethod method;
	private int index;

	public OwnParQuery(SootMethod method,int index){
		this.method = method;
		this.index = index;
		ID = 10;
	}

	public SootMethod getMethod(){
		return method;
	}
	public boolean alwaysTrue(){
		if(method.getParameterType(index) instanceof PrimType) return true;
		if(method.getParameterType(index) instanceof RefType &&
				((RefType)method.getParameterType(index)).getClassName().equals("java.lang.String"))
			return true;
		return false;
	}

	/*
	 *  (non-Javadoc)
	 * @see uno.query.Query#getQueries()
	 * 1. Ownership of parameter (m,i)
	Depends on 
	- sub
	- Freshness of parameter (m,i)
	- Non-escaping of parameter (m,i)
	- if super[j] ^ ARG[i] != 0, Ownership of (super,j)
	- if this[j] ^ ARG[i] != 0, Ownership of (this,j)
		(To implement the above 2, treat super() and this() as NOT
		invocations)
	- for all c s.t. m=super/this of c, 
		Ownership of parameter (m,i) w.r.t. c
	- if superthis = null, Store(m,i)
	 */
	protected List[] getQueries() {
		// TODO Auto-generated method stub

		List[] queries = createListArray(5);

		//queries[1].add(QueryFactory.getFreshQuery(method,index,false));
		queries[1].add(QueryFactory.getNotLeakParQuery(method,index));


		boolean needStore = true;
		
		for(Iterator i=uno.Hierarchy.getSubMethodOf(method).iterator();i.hasNext();){
			SootMethod subm = (SootMethod) i.next();
			queries[0].add(QueryFactory.getOwnParQuery(subm,index));
		}
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		SootMethod superORthis = p.get_superORthis();
		if(superORthis!=null){
			for(int i=superORthis.getParameterCount()-1;i>=0;i--){
				if(share(p.getPar(index),p.get_superORthis_par_TO_obj(superORthis,i))){
					queries[3].add(QueryFactory.getOwnParQuery(superORthis,i));
					needStore = false;
				}
			}
		}
		if(needStore) queries[2].add(QueryFactory.getStoreQuery(method,index));
		
		// removed 0907
		//for(Iterator i=CallGraph.getSuperthisCallersOf(method).iterator();i.hasNext();){
		//	SootMethod caller = (SootMethod) i.next();
		//	PointsToAnalysis pc = PointsToAnalysisFactory.getInstance(caller);
		//	if(pc.get_superORthis()!=null && pc.get_superORthis().equals(method)){
		//		
		//		queries.add(QueryFactory.getOwnInQuery(method,index,caller));
		//	}
		//}
		
		return queries;

	}

	public boolean thisLocalIsFalse(int rule){
		return false;
	}
	protected boolean isLocallyFalse() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCalled(){
		return CallGraph.getCallersOf(method).size()>0;
	}
	protected void setName() {
		// TODO Auto-generated method stub
		boolean isCalled = (CallGraph.getCallersOf(method).size()==0)&&(CallGraph.getSuperthisCallersOf(method).size()==0);
		boolean calledBySupThis = CallGraph.getSuperthisCallersOf(method).size()!=0;
		name = "OwnPar ( " + simplify(method) + " , " + index + " ) " + (isCalled?" *never called* ":" *called* ") ;
	}
}
