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
 * Created on Apr 13, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package uno.query;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.SootField;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import uno.analysis.PointsToAnalysis;
import uno.analysis.PointsToAnalysisFactory;

/**
 * @author kkma
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UniqBaseQuery extends Query {

	private SootMethod method;

	/**
	 * @param method
	 */
	public UniqBaseQuery(SootMethod method) {
		this.method = method;
		ID = 2;
		// TODO Auto-generated constructor stub
	}
	public SootMethod getMethod(){return method;}

	/* (non-Javadoc)
	 * @see uno.query.Query#getQueries()
	 */
	public boolean alwaysTrue(){return false;}

	protected List[] getQueries() {
		List[] queries = createListArray(5);

		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		for(Iterator i=uno.Hierarchy.getSubMethodOf(method).iterator();i.hasNext();){
			SootMethod subm = (SootMethod) i.next();
			queries[0].add(QueryFactory.getUniqBaseQuery(subm));
		}
		// TODO Auto-generated method stub
		for(Iterator j=p.getInvocations().iterator();j.hasNext();){
			InvokeExpr inv = (InvokeExpr) j.next();
			for(int i=0;i<inv.getArgCount();i++){
				if(share(p.getThis(),p.getInvocationPar(inv,i)))
					queries[1].add(QueryFactory.getUniqQuery(inv,i));
				// added 0907
				if(share(p.getThis(),p.getInvocationBase(inv)))
					queries[2].add(QueryFactory.getUniqBaseQuery(inv));
			}
		}
		// added 0907
		SootMethod superthis = p.get_superORthis();
		if(superthis!=null){
			for(int j=0;j<superthis.getParameterCount();j++){
				if(share(p.getThis(),p.get_superORthis_par_TO_obj(superthis,j)))
					queries[3].add(QueryFactory.getUniqQuery(superthis,j));
			}
			queries[4].add(QueryFactory.getUniqBaseQuery(superthis));
		}
		
								
		return queries;
	}

	/* (non-Javadoc)
	 * @see uno.query.Query#isLocallyFalse()
	 */
	protected boolean isLocallyFalse() {
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		
			if(share(p.getReturn(),p.getThis()))
				return true;
			if(share(p.getThis(),p.getBad()))
				return true;
			for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
				SootField f = (SootField) i.next();
				if(share(p.getPointedByField(f),p.getThis())) return true;
			}

		return false;
	}

	public boolean thisLocalIsFalse(int rule){
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		
			if(rule==1 && share(p.getReturn(),p.getThis()))
				return true;
			if(rule==0 && share(p.getThis(),p.getBad()))
				return true;
			if(rule==2)
			for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
				SootField f = (SootField) i.next();
				if(share(p.getPointedByField(f),p.getThis())) return true;
			}

		return false;

	}

	/* (non-Javadoc)
	 * @see uno.query.Query#setName()
	 */
	protected void setName() {
		// TODO Auto-generated method stub
		name = "LentBase ( " + simplify(method) + " )";
	}

}
