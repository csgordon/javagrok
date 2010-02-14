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

import soot.*;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import uno.analysis.PointsToAnalysis;
import uno.analysis.PointsToAnalysisFactory;
import uno.query.Query;
import uno.toolkit.*;
import uno.*;

/**
 * 4. Uniqueness of parameter (m,i)
	Depends on
	- ARG[i] ^ RET = 0 (*)
	- ARG[i] ^ BAD = 0
	- ARG[i] ^ FIELD[f] = 0 for all f
	- for all inv, j s.t. ARG[i] ^ ARG(inv)[j] != 0,
		Uniqueness of parameter (method(inv),j)

   Uniqueness of return (m)
	Depends on
	- RET ^ THIS = 0
	- RET ^ BAD = 0
	- RET ^ FIELD[f] = 0 for all f
	- for all i s.t. RET ^ ARG[i] != 0,
		Freshness of parameter (m,i)
	- for all inv, i s.t. RET ^ ARG(inv)[i] != 0,
		Uniqueness of parameter (method(inv),i)

 * @author kkma
 *
 */
public class UniqQuery extends Query {

	public SootMethod method;
	private int index;
	private boolean needed;


	public UniqQuery(SootMethod method, int index) {
		this(method,index,true);
	}
	public UniqQuery(SootMethod method, int index,boolean b) {
		// TODO Auto-generated constructor stub
		this.method = method;
		this.index = index;
		needed = b;
		if(index<0)
		{
			ID = 0;
			super.isUniqReturn = true;
		}
		else
			ID = 1;
	}
	public boolean alwaysTrue(){
		if(index<0) {

			
			return false;
		}
		if(method.getParameterType(index) instanceof PrimType) return true;
		if(method.getParameterType(index) instanceof RefType &&
				((RefType)method.getParameterType(index)).getClassName().equals("java.lang.String"))
			return true;
		return false;
	}


	public SootMethod getMethod(){return method;}
	public boolean getStrict(){ return needed;}
	public boolean isUniqRet(){ return index<0;}

	protected List[] getQueries() {
		// TODO Auto-generated method stub
		
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		if(index<0) /* return */
		{
			List[] queries = createListArray(6);
			// sub
			for(Iterator i=uno.Hierarchy.getSubMethodOf(method).iterator();i.hasNext();){
				SootMethod subm = (SootMethod) i.next();
				queries[0].add(QueryFactory.getUniqQuery(subm,index));
			}
			/*
			 * 	- for all i s.t. RET ^ ARG[i] != 0,
					Freshness of parameter (m,i)
				- for all inv, i s.t. RET ^ ARG(inv)[i] != 0,
					Uniqueness of parameter (method(inv),i)
				- for all inv s.t. RET ^ RET(inv) !=0,
					Uniqueness of return (method(inv))
			 */
			for(int i=0;i<method.getParameterCount();i++){
				if(share(p.getReturn(),p.getPar(i)))
					queries[1].add(QueryFactory.getFreshQuery(method,i));
			}
			for(Iterator j=p.getInvocations().iterator();j.hasNext();){
				InvokeExpr inv = (InvokeExpr) j.next();
				if(share(p.getReturn(),singleton(inv)))
					queries[2].add(QueryFactory.getUniqQuery(inv,-1));
				if(share(p.getReturn(),p.getInvocationBase(inv)))
					queries[4].add(QueryFactory.getUniqBaseQuery(inv));
				for(int i=0;i<inv.getArgCount();i++){
					if(share(p.getReturn(),p.getInvocationPar(inv,i)))
						queries[3].add(QueryFactory.getUniqQuery(inv,i));
				}
			}
			// added 0908 : if m is constructor, LentBase(m)
			if(method.getName().equals("<init>"))
				queries[5].add(QueryFactory.getUniqBaseQuery(method));
			return queries;
		}
		else{
			//LentPar
			List[] queries = createListArray(5);
			// sub
			for(Iterator i=uno.Hierarchy.getSubMethodOf(method).iterator();i.hasNext();){
				SootMethod subm = (SootMethod) i.next();
				queries[0].add(QueryFactory.getUniqQuery(subm,index));
			}
			Type t = method.getParameterType(index);
			if(t instanceof ArrayType)
			{
				/*
				 *  - for all inv, s.t. ARG[i] ^ RET(inv) != 0 && ARG[i] is array
				 *  	Uniqueness (method(inv))
				 */
				for(Iterator j=p.getInvocations().iterator();j.hasNext();){
					InvokeExpr inv = (InvokeExpr) j.next();
					if(share(p.getPar(index),p.getInvocationReturn(inv)))
							queries[4].add(QueryFactory.getUniqQuery(inv,-1));
				}
				/*
				 *  - for all j, s.t. ARG[i] ^ ARG[j] != 0 && ARG[i] is array
				 *  	fresh (m,j)
				 */
				for(int j=0;j<method.getParameterCount();j++)
					if(j!=index && share(p.getPar(index),p.getPar(j)))
							queries[4].add(QueryFactory.getFreshQuery(method,j));
			}
			
			/*
			 * 	- for all inv, j s.t. ARG[i] ^ ARG(inv)[j] != 0,
					Uniqueness of parameter (method(inv),j)
			 */
			for(Iterator j=p.getInvocations().iterator();j.hasNext();){
				InvokeExpr inv = (InvokeExpr) j.next();
				for(int i=0;i<inv.getArgCount();i++){
					if(share(p.getPar(index),p.getInvocationPar(inv,i)))
						queries[1].add(QueryFactory.getUniqQuery(inv,i));
				}
			}
			/*
			 * for all j, superthis(j) ^ par(i) !=0, UniqPar(superthis,j)
			 */
			if(p.get_superORthis()!=null)
			for(int j=0;j<p.get_superORthis().getParameterCount();j++){
				if(share(p.getPar(index),p.get_superORthis_par_TO_obj(p.get_superORthis(),j)))
					queries[3].add(QueryFactory.getUniqQuery(p.get_superORthis(),j,needed));
			}
			// suggested by jfoster 0903: forall l s.t. {lpi} ^ base(l) != empty
			// uniqNCbase(method(l))
			for(Iterator j=p.getInvocations().iterator();j.hasNext();){
				InvokeExpr inv = (InvokeExpr) j.next();
				if(share(p.getPar(index),p.getInvocationBase(inv)))
					queries[2].add(QueryFactory.getUniqBaseQuery(inv));
			}
			return queries;
		}
	}

	public boolean isCalled(){
	       return CallGraph.getCallersOf(method).size()>0;                                              
	}                 
	
	public boolean thisLocalIsFalse(int rule){
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		
		if(index<0) /* return */
		{
			switch(rule){
				case 0: return share(p.getReturn(),p.getBad());
				case 1: return share(p.getReturn(),p.getThis());
				case 2:
						for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
							SootField f = (SootField) i.next();
							if(share(p.getPointedByField(f),p.getReturn())) return true; 
						}
			}
		}
		else{
			switch(rule){
				case 0: return share(p.getPar(index),p.getBad());
				case 1: return share(p.getPar(index),p.getReturn());
				case 2:
						if(needed)
						for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
							SootField f = (SootField) i.next();
							if(share(p.getPointedByField(f),p.getPar(index))) return true;
						}
			}
		}
		return false;

	}


	protected boolean isLocallyFalse() {
		// TODO Auto-generated method stub
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		
		if(index<0) /* return */
		{
			if(share(p.getReturn(),p.getThis()))
				return true;
			if(share(p.getReturn(),p.getBad()))
				return true;
			for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
				SootField f = (SootField) i.next();
				if(share(p.getPointedByField(f),p.getReturn())) return true;
			}
		}
		else{
			if(share(p.getPar(index),p.getBad()) ||
			   share(p.getPar(index),p.getReturn()))
				return true;
			if(needed)
			for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
				SootField f = (SootField) i.next();
				if(share(p.getPointedByField(f),p.getPar(index))) return true;
			}
			Type t = method.getParameterType(index);
			if(t instanceof ArrayType)
			{
				// no this, 
				if(share(p.getPar(index),p.getThis())) return true;
			}
		}
		return false;
	}

	protected void setName() {
		// TODO Auto-generated method stub
		if(index<0) // UniqRet
			name = "UniqRet ( " + simplify(method) + " )";
		else // LentPar
			name = "LentPar ( " + simplify(method) + " , " + index + " , " + needed + " )";
	}

}
