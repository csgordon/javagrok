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
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import uno.analysis.PointsToAnalysis;
import uno.analysis.PointsToAnalysisFactory;

public class FFieldInQuery extends Query {

	private SootField f;
	private SootMethod method;

	public FFieldInQuery(SootField f, SootMethod method) {
		// TODO Auto-generated constructor stub
		this.f=f;
		this.method = method;
		ID = 8;
	}
	public boolean alwaysTrue(){
		return false;
	}

	public SootField getField(){ return f;}
	public SootMethod getMethod(){return method;}

	protected List[] getQueries() {
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		List[] queries = createListArray(3);
		for(Iterator i=p.getInvocations().iterator();i.hasNext();){
			InvokeExpr inv = (InvokeExpr) i.next();
			for(int k=0;k<inv.getArgCount();k++){
				if(share(p.getInvocationPar(inv,k),p.getPointedByField(f))){
//					if(inv instanceof SpecialInvokeExpr){
//						
//					}else{
//						
//					}
					queries[0].add(QueryFactory.getUniqQuery(inv,k));
				}
			}
			//if(share(p.getInvocationReturn(inv),p.getPointedByField(f))){
			//	queries[0].add(QueryFactory.getUniqQuery(inv,-1));
			//}
			if(share(p.getInvocationBase(inv),p.getPointedByField(f))){
				queries[1].add(QueryFactory.getUniqBaseQuery(inv));
			}
		}
		// added 0907
		SootMethod superthis = p.get_superORthis();
		if(superthis!=null){
			for(int j=0;j<superthis.getParameterCount();j++){
				if(share(p.getPointedByField(f),p.get_superORthis_par_TO_obj(superthis,j)))
					queries[2].add(QueryFactory.getUniqQuery(superthis,j));
			}
			queries[1].add(QueryFactory.getUniqBaseQuery(superthis));
		}
		return queries;
	}

	public boolean thisLocalIsFalse(int rule){
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		// f not in BADFIELDS 
		if(rule==2&&p.getBadFields().contains(f)) return true;
		
		//System.out.println("isShare? " + p.getPointedByField(f) + " , " + p.getReturn());
		if(rule==0&&share(p.getPointedByField(f),p.getBad()))
			return true;
		//if(rule==2&&share(p.getPointedByField(f),p.getThis()) )
		//	return true;
		if(rule==1&&share(p.getPointedByField(f),p.getReturn()))
			return true;
		else return false;
	}

	protected boolean isLocallyFalse() {
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		// f not in BADFIELDS 
		if(p.getBadFields().contains(f)) return true;
		
		//System.out.println("isShare? " + p.getPointedByField(f) + " , " + p.getReturn());
		if(share(p.getPointedByField(f),p.getBad()) ||
			//share(p.getPointedByField(f),p.getThis()) ||
			share(p.getPointedByField(f),p.getReturn()))
			return true;
		else return false;
	}

	protected void setName() {
		// TODO Auto-generated method stub
		name = "NEscField-In ( " + simplify(f) + " , " + simplify(method) + " )";
	}

}
