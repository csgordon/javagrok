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

public class StoreQuery extends Query{
	
	private SootMethod method;
	private int index;

	public StoreQuery(SootMethod method,int index){
		this.method = method;
		this.index = index;
		ID = 9;
	}

	public SootMethod getMethod(){return method;}
	protected List[] getQueries() {
		// TODO Auto-generated method stub
		List queries = new LinkedList();
		for(Iterator i=uno.Hierarchy.getSubMethodOf(method).iterator();i.hasNext();){
			SootMethod subm = (SootMethod) i.next();
			queries.add(QueryFactory.getStoreQuery(subm,index));
		}
		        List[] r = new List[1];
				        r[0] = queries;
						        return r;

	}
	public boolean alwaysTrue(){
		if(method.getParameterType(index) instanceof PrimType) return true;
		if(method.getParameterType(index) instanceof RefType &&
				((RefType)method.getParameterType(index)).getClassName().equals("java.lang.String"))
			return true;
		return false;
	}

	public boolean thisLocalIsFalse(int rule){
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
			SootField f = (SootField) i.next();
			if(share(p.getPar(index),p.getPointedByField(f)))
				return false;
		}
		if(p.get_superORthis()!=null){
			for(int j=0;j<p.get_superORthis().getParameterCount();j++)
				if(share(p.getPar(index),p.get_superORthis_par_TO_obj(p.get_superORthis(),j)))
					return false;
		}
		return true;
	}

	protected boolean isLocallyFalse() {
		PointsToAnalysis p = PointsToAnalysisFactory.getInstance(method);
		for(Iterator i=method.getDeclaringClass().getFields().iterator();i.hasNext();){
			SootField f = (SootField) i.next();
			if(share(p.getPar(index),p.getPointedByField(f)))
				return false;
		}
		if(p.get_superORthis()!=null){
			for(int j=0;j<p.get_superORthis().getParameterCount();j++)
				if(share(p.getPar(index),p.get_superORthis_par_TO_obj(p.get_superORthis(),j)))
					return false;
		}
		return true;
	}

	protected void setName() {
		// TODO Auto-generated method stub
		name = "Store ( " + simplify(method) + " , " + index;
	}
}
