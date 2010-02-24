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
import uno.analysis.PointsToAnalysis;
import uno.analysis.PointsToAnalysisFactory;

/*
6. Freshness of parameter wrt method m (m,i,c)
	Depends on
	- for all inv s.t. method(inv)=c,
		BAD(inv) ^ ARG(inv)[i] = 0
		f[?] ^ ARG(inv)[i] = 0
		THIS ^ ARG(inv)[i] = 0    ( q: need to consider constructor? )
		BAD ^ ARG(inv)[i] = 0
		for all j s.t. ARG[j] ^ ARG(inv)[i] != 0
			Freshness of (m,j)
		for all inv2 s.t. RET(inv2) ^ ARG(inv)[i],
			Uniqueness of return (method(inv2))
TODO:	for all inv2,j s.t. ARG(inv2)[j] ^ ARG(inv)[i] && inv2 in before(inv),
			if (inv2,j) != (inv,i),
				Uniqueness of parameter (method(inv2),j)
		similar for superthis
 */
public class FreshInQuery extends Query {

	private SootMethod method;
	private int index;
	private SootMethod caller;
	private boolean needed;
	
	public SootMethod getMethod(){
		return method;
	}
	public boolean getStrict(){ return needed;}

	public FreshInQuery(SootMethod method, int index, SootMethod caller) {
		this(method,index,caller,true);
	}
	public FreshInQuery(SootMethod method, int index, SootMethod caller,boolean needed) {
		// TODO Auto-generated constructor stub
		this.method = method;
		this.index = index; 
		this.caller = caller;
		this.needed = needed;
		ID = 4;
	}
	public boolean alwaysTrue(){return false;}

	protected List[] getQueries() {
		// TODO Auto-generated method stub
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(caller);
		List[] queries = createListArray(6);

		for(int j=0;j<caller.getParameterCount();j++){
			if(share(p.getPar(j),p.getCalleePar(method,index))){
				queries[0].add(QueryFactory.getFreshQuery(caller,j));
			}
		}
		for(Iterator i=p.getCallees().iterator();i.hasNext();){
			SootMethod m = (SootMethod) i.next();
			if(m==method)continue;
			if(share(p.getCalleePar(method,index),p.getCalleeReturn(m))){
				queries[1].add(QueryFactory.getUniqQuery(m,-1));
			}	
		}
		
//		for(Iterator i=p.getCallees().iterator();i.hasNext();){
//			SootMethod m = (SootMethod) i.next();
//			if(m==method)continue;
//			for(int k=0;k<m.getParameterCount();k++){
//				//TODO:	for all inv2,j s.t. ARG(inv2)[j] ^ ARG(inv)[i] && inv2 in before(inv),
//				if(share(p.getCalleePar(method,index),p.getCalleePar(m,k))){
//					queries.add(QueryFactory.getUniqQuery(m,k));
//				}
//			}
//		}
		// TODO: relax the requirements
		
		for(Iterator i=p.getInvocations().iterator();i.hasNext();){
			InvokeExpr inv = (InvokeExpr) i.next();
			try{
				inv.getMethod();			
			}catch(Exception e){
				queries[5].add(new FalseQuery(inv));
				break;
			}
			if(inv.getMethod().equals(method)){
				for(Iterator j=p.getInvocationsBefore(inv).iterator();j.hasNext();){
					InvokeExpr inv2 = (InvokeExpr) j.next();
					
					for(int k=0;k<inv2.getArgCount();k++){
						//TODO:	for all inv2,j s.t. ARG(inv2)[j] ^ ARG(inv)[i] && inv2 in before(inv),
						if(share(p.getInvocationPar(inv,index),p.getInvocationBase(inv2))){
							try{
								inv2.getMethod();			
							}catch(Exception e){
								queries[5].add(new FalseQuery(inv2));
								break;
							}
							queries[3].add(QueryFactory.getUniqBaseQuery(inv2.getMethod()));
						}
					}
					
					
					//if(inv.equals(inv2)) continue;
					//if(caller.toString().equals("Collections$SynchronizedRandomAccessList::void <init>(List)"))
//					System.out.println(caller);
//						System.out.println("    "+inv2 + " invokes before " + inv);
					for(int k=0;k<inv2.getArgCount();k++){
						//TODO:	for all inv2,j s.t. ARG(inv2)[j] ^ ARG(inv)[i] && inv2 in before(inv),
						if(share(p.getInvocationPar(inv,index),p.getInvocationPar(inv2,k))){
							try{
								inv2.getMethod();			
							}catch(Exception e){
								queries[5].add(new FalseQuery(inv2));
								break;
							}
							queries[2].add(QueryFactory.getUniqQuery(inv2.getMethod(),k));
						}
					}
				}
			}
			
		}	
		
		if(p.get_superORthis()!=null){
			for(int j=0;j<p.get_superORthis().getParameterCount();j++)
				if(share(p.get_superORthis_par_TO_obj(p.get_superORthis(),j),p.getCalleePar(method,index)))
					queries[4].add(QueryFactory.getUniqQuery(p.get_superORthis(),j,needed));
		}
		return queries;
	}

	public boolean thisLocalIsFalse(int rule){
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(caller);
		
		if(rule==0&&share(p.getCalleePar(method,index),p.getBad()) )
		    return true;
		else if(rule==1&&share(p.getCalleePar(method,index),p.getThis())) //************************
			return true;
		if(rule==2&&needed)
		if( share(p.getCalleePar(method,index),p.getCalleeLiveAfter(method)) )
			return true;

		// f[?] ^ ARG(inv)[i] = 0
		if(rule==3&&needed)
		for(Iterator i=caller.getDeclaringClass().getFields().iterator();i.hasNext();){
			SootField f = (SootField) i.next();
			if(share(p.getCalleePar(method,index),p.getPointedByField(f)))
				return true;
		}


		if(rule==4)
		{
		int nPar = method.getParameterCount();
		for(int i=0;i<nPar;i++){
			if(i==index) continue;
			if(share(p.getCalleePar(method,index),p.getCalleePar(method,i)))
				return true;
		}
		}
		if(rule==5)
		{
		for(Iterator i=p.getInvocations().iterator();i.hasNext();){
			InvokeExpr inv = (InvokeExpr) i.next();
			try{
				inv.getMethod();			
			}catch(Exception e){
				continue;
			}
			if(inv.getMethod().equals(method)){
				if(share(p.getInvocationPar(inv,index),p.getInvocationBase(inv))){
					return true;
				}
			}
		}
		}

		return false;

	}
	protected boolean isLocallyFalse() {
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(caller);
		
		
		if(share(p.getCalleePar(method,index),p.getBad()) ||
		   share(p.getCalleePar(method,index),p.getThis())) //************************
			return true;
		if(needed)
		if(
		   share(p.getCalleePar(method,index),p.getCalleeLiveAfter(method)) )
			return true;
		// f[?] ^ ARG(inv)[i] = 0
		if(needed)
		for(Iterator i=caller.getDeclaringClass().getFields().iterator();i.hasNext();){
			SootField f = (SootField) i.next();
			if(share(p.getCalleePar(method,index),p.getPointedByField(f)))
				return true;
		}
		//4
		int nPar = method.getParameterCount();
		for(int i=0;i<nPar;i++){
			if(i==index) continue;
			if(share(p.getCalleePar(method,index),p.getCalleePar(method,i)))
				return true;
		}
		//5
		for(Iterator i=p.getInvocations().iterator();i.hasNext();){
			InvokeExpr inv = (InvokeExpr) i.next();
			try{
				inv.getMethod();			
			}catch(Exception e){
				continue;
			}
			if(inv.getMethod().equals(method)){
				if(share(p.getInvocationPar(inv,index),p.getInvocationBase(inv))){
					return true;
				}
			}
		}

		return false;
	}

	protected void setName() {
		// TODO Auto-generated method stub
		name = "UniqPar-In ( " + simplify(caller) + " , " + (index) + " , " + simplify(method) + " , " + (needed) + " )";
	}

}
