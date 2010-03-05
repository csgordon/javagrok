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

public class OwnFieldInQuery extends Query {

	private SootField f;
	private SootMethod method;

	public OwnFieldInQuery(SootField f, SootMethod method) {
		// TODO Auto-generated constructor stub
		this.f=f;
		this.method = method;
		ID = 12;
	}
	public boolean alwaysTrue(){
		return false;
	}

	public SootMethod getMethod(){return method;}
	public SootField getField(){return f;}

	protected List[] getQueries() {
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		List[] queries = createListArray(3);

		for(int i=method.getParameterCount()-1;i>=0;i--){
			int index = i;
            if(share(p.getPar(index),p.getPointedByField(f)))
				// change no-fld to be false 0729
                //queries[0].add(QueryFactory.getFreshQuery(method,index,true));
                queries[0].add(QueryFactory.getFreshQuery(method,index,false));
        }
		for(Iterator i=f.getDeclaringClass().getFields().iterator();i.hasNext();){
			SootField g = (SootField) i.next();
			if(g.equals(f)) continue;
			if(share(p.getPointedByField(f),p.getPointedByField(g)))
				queries[1].add(QueryFactory.getOwnFieldQuery(g));
		}
		for(Iterator i=p.getInvocations().iterator();i.hasNext();){
			InvokeExpr inv = (InvokeExpr) i.next();
			if(share(p.getInvocationReturn(inv),p.getPointedByField(f))){
				queries[2].add(QueryFactory.getUniqQuery(inv,-1));
			}
		}


		return queries;
	}

	public boolean thisLocalIsFalse(int rule){
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		if(rule==0&&share(p.getPointedByField(f),p.getThis()) )
			return true;
		else
			return false;
	}

	protected boolean isLocallyFalse() {
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		if(share(p.getPointedByField(f),p.getThis()) )
			return true;
		return false;
	}

	protected void setName() {
		// TODO Auto-generated method stub
		name = "OwnField-In ( " + simplify(f) + " , " + simplify(method) + " )";
	}

}
