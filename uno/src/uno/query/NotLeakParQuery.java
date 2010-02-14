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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.*;
import soot.SootMethod;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import uno.analysis.PointsToAnalysis;
import uno.analysis.PointsToAnalysisFactory;

/**
 * 
 * @author kkma
 *
 *3. Non-escaping parameter (m,i)
	Depends on
	- RET ^ ARG[i] = 0
	- BAD ^ ARG[i] = 0
	- for all f s.t. FIELD[f] ^ ARG[i] != 0,
		Non-escaping field (f)
	- for all inv, j s.t. ARG(inv)[j] ^ ARG[i],
		Non-escaping parameter (inv,j)
		RET ^ base(inv) = 0
		BAD ^ base(inv) = 0
		for all f s.t. FIELD[f] ^ base(inv) !=0,
			Non-escaping field (f)

 */
public class NotLeakParQuery extends Query {

	private int index;
	private SootMethod method;

	public NotLeakParQuery(SootMethod method, int index) {
		// TODO Auto-generated constructor stub
		this.method = method;
		this.index = index;
		ID = 6;
	}
	public boolean alwaysTrue(){
		if(method.getParameterType(index) instanceof PrimType) return true;
		if(method.getParameterType(index) instanceof RefType &&
				((RefType)method.getParameterType(index)).getClassName().equals("java.lang.String"))
			return true;
		return false;
	}

	public SootMethod getMethod(){return method;}
	protected List[] getQueries() {
		// TODO Auto-generated method stub
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		List[] queries = createListArray(5);
		/*
		 * 	- for all f s.t. FIELD[f] ^ ARG[i] != 0,
		 		Non-escaping field (f)
		 */
		for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
			SootField f = (SootField) i.next();
			
			if(share(p.getPar(index),p.getPointedByField(f)))
				queries[4].add(QueryFactory.getFFieldQuery(f));
		}
		/*
		 * 	- for all inv, j s.t. ARG(inv)[j] ^ ARG[i],
				Uniquesness of parameter (inv,j)
				for all f s.t. FIELD[f] ^ base(inv) !=0,
					Non-escaping field (f)
		 */
		for(Iterator i=p.getInvocations().iterator();i.hasNext();){
			InvokeExpr inv = (InvokeExpr) i.next();
			for(int j=0;j<inv.getArgCount();j++)
			{
				if(share(p.getInvocationBase(inv),p.getPar(index)))
					queries[2].add(QueryFactory.getUniqBaseQuery(inv));

				if(share(p.getInvocationPar(inv,j),p.getPar(index))){
					queries[1].add(QueryFactory.getUniqQuery(inv,j));
					// removed 0905
					//for(Iterator k=method.getDeclaringClass().getFields().iterator();k.hasNext();){
					//	SootField f = (SootField) k.next();
					//	
					//	if(share(p.getPointedByField(f),p.getInvocationBase(inv)))
					//		queries.add(QueryFactory.getFFieldQuery(f));
					//}
				}
			}
		}
		// for the superthis call, do UniqNCPar(superthis,j,false)
		if(p.get_superORthis()!=null)
			for(int j=0;j<p.get_superORthis().getParameterCount();j++)
				if(share(p.get_superORthis_par_TO_obj(p.get_superORthis(),j),p.getPar(index))){
					queries[3].add(QueryFactory.getUniqQuery(p.get_superORthis(),j,false));
				}
		
		for(Iterator i=uno.Hierarchy.getSubMethodOf(method).iterator();i.hasNext();){
			SootMethod subm = (SootMethod) i.next();
			queries[0].add(QueryFactory.getNotLeakParQuery(subm,index));
		}

		return queries;
	}

	 public boolean thisLocalIsFalse(int rule){
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		if(rule==1&&share(p.getReturn(),p.getPar(index))) return true;
		if(rule==0&&share(p.getBad(),p.getPar(index))) return true;
		return false;
	}
	protected boolean isLocallyFalse() {
		// TODO Auto-generated method stub
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		if(share(p.getReturn(),p.getPar(index))) return true;
		if(share(p.getBad(),p.getPar(index))) return true;
		return false;
	}

	protected void setName() {
		// TODO Auto-generated method stub
		name = "NON-LEAKING PARAMETER of " + simplify(method) + " " + (index<0?"return value":"parameter "+index) ;
		name = "NEscPar ( " + simplify(method) + " , " + (index) + " )";
	}

}
