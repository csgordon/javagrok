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

package uno.analysis;

import java.util.AbstractSequentialList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.SootField;
import soot.RefLikeType;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.ExceptionalUnitGraph;
import uno.toolkit.LazySet;
import uno.toolkit.Msg;
import uno.toolkit.*;

public class PointsToAnalysis {

	private boolean failed = false;

	private SootMethod method;
	private PointsToFlowAnalysis analysis;

	private Set callees = new HashSet();
	private Set invocations = new HashSet();
	private Map calleeToProperty = new HashMap();
	private Map invocationToProperty = new HashMap();
	private Map fieldToSet = new HashMap();
	private PropertySets myPropertySets = null;
	private LazySet thisSet = new LazySet("ThisSet"); 
	private LazySet badFields = new LazySet("badfields"); 
	private Map superORthis_par_TO_obj = new HashMap();
	private SootMethod superORthis = null;
	private Map invToInvocationsBefore;
	private int mysize = 0;
	private LazySet allInvocations = null;

	private static final int SIZE_THRESHOLD = 100;
	private int n_locals = 0;

	private ExceptionalUnitGraph g = null;
	public ExceptionalUnitGraph getUnitGraph(){ return g; }
	
	public PointsToAnalysis(SootMethod method){
		this.method = method;
		try{
			
			this.myPropertySets = new PropertySets("this",method.getParameterCount());

			Timer.start(1);			
			JimpleBody body = (JimpleBody) method.retrieveActiveBody();
			//System.out.print("(");
			//System.out.print("("+  body.getUnits().size() +" loc)");
			g = new ExceptionalUnitGraph(body);
			Timer.finish(1);		
			
			mysize = g.size();		
			this.analysis = new PointsToFlowAnalysis(g);
			//System.out.print(")");
			 callees = analysis.callees ;
			 invocations = analysis.invocations ;
			 calleeToProperty = analysis.calleeToProperty ;
			 invocationToProperty = analysis.invocationToProperty ;
			 fieldToSet = analysis.fieldToSet ;
			 myPropertySets = analysis.myPropertySets ;
			 thisSet = analysis.thisSet ; 
			 badFields = analysis.badFields ; 
			 superORthis_par_TO_obj = analysis.superORthis_par_TO_obj ;
			 superORthis = analysis.superORthis ;
			 invToInvocationsBefore = analysis.invToInvocationsBefore;
			
			this.analysis = null;	//*****************************************
		

			for(Iterator i=body.getLocals().iterator();i.hasNext();){
				Local l = (Local)i.next();
				if(l.getType() instanceof RefLikeType)
					n_locals++;
			}

			
			method.releaseActiveBody();
			//Msg.println(this);
		}catch(Exception e){ 
			failed = true;
			// two possibilities:
			// 1. method is abstract/native
			// 2. method contains a field reference of phantom class
			//
			//e.printStackTrace();
		}
	}
	public int getLocalCount() { return n_locals; }

	public boolean isFailed(){ return failed; }
	
	public LazySet getReturn(){
		return this.myPropertySets.getRET();
	}
	
	public LazySet getBad(){
		return this.myPropertySets.getBAD();
	}
	
	public LazySet getPar(int index){
		return this.myPropertySets.getPAR(index);
	}
	
	public LazySet getThis(){
		return this.thisSet;
	}
	public LazySet getPointedByField(SootField field){
		LazySet s =(LazySet) this.fieldToSet.get(field);
		if(s==null) return new LazySet("getPointedByField");
		return s;
	}
	
	public Set getCallees(){
		return this.callees;
	}
	
	public Set getInvocations(){
		return this.invocations;
	}
	
	public LazySet getCalleeReturn(SootMethod callee){
		if(!calleeToProperty.containsKey(callee))
			calleeToProperty.put(callee,new PropertySets(callee.toString(),callee.getParameterCount()));
		return ((PropertySets) calleeToProperty.get(callee)).getRET();
	}
	
	public LazySet getCalleePar(SootMethod callee,int index){
		if(!calleeToProperty.containsKey(callee))
			calleeToProperty.put(callee,new PropertySets(callee.toString(),callee.getParameterCount()));
		return ((PropertySets) calleeToProperty.get(callee)).getPAR(index);
	}
	
	public LazySet getCalleeLiveAfter(SootMethod callee){
		if(!calleeToProperty.containsKey(callee))
			calleeToProperty.put(callee,new PropertySets(callee.toString(),callee.getParameterCount()));
		return ((PropertySets) calleeToProperty.get(callee)).getBAD();
	}
	
	public LazySet getInvocationBase(InvokeExpr inv){
		if(!invocationToProperty.containsKey(inv))
			invocationToProperty.put(inv,new PropertySets(inv.toString(),inv.getMethod().getParameterCount()));
		return ((PropertySets) invocationToProperty.get(inv)).getBase();
	}
	public LazySet getInvocationReturn(InvokeExpr inv){
		if(!invocationToProperty.containsKey(inv))
			invocationToProperty.put(inv,new PropertySets(inv.toString(),inv.getMethod().getParameterCount()));
		return ((PropertySets) invocationToProperty.get(inv)).getRET();
	}
	
	public LazySet getInvocationPar(InvokeExpr inv,int index){
		if(!invocationToProperty.containsKey(inv))
			invocationToProperty.put(inv,new PropertySets(inv.toString(),inv.getMethod().getParameterCount()));
		return ((PropertySets) invocationToProperty.get(inv)).getPAR(index);
	}
	
	public LazySet getInvocationLiveAfter(InvokeExpr inv){
		if(!invocationToProperty.containsKey(inv))
			invocationToProperty.put(inv,new PropertySets(inv.toString(),inv.getMethod().getParameterCount()));
		return ((PropertySets) invocationToProperty.get(inv)).getBAD();
	}
	public LazySet getBadFields() {
		return this.badFields;
	}
	
	public LazySet get_superORthis_par_TO_obj(SootMethod superORthis,int index){
		String key = superORthis.toString() + index;
		if(!this.superORthis_par_TO_obj.containsKey(key)){
			this.superORthis_par_TO_obj.put(key,new LazySet("superORthis_par_TO_obj"));
		}
		return (LazySet) this.superORthis_par_TO_obj.get(key);
	}
	
	public SootMethod get_superORthis(){
		return this.superORthis;
	}
	/**
	 * @param inv
	 * @return
	 */
	public LazySet getInvocationsBefore(InvokeExpr inv) {
		//if(mysize>SIZE_THRESHOLD) {
		//	if(allInvocations==null){
		//		allInvocations = new LazySet("all invocations");
		//		for(Iterator i=this.getInvocations().iterator();i.hasNext();)
		//			allInvocations.add(i.next());
		//	}
		//	return allInvocations;
		//}
		LazySet s = (LazySet) this.invToInvocationsBefore.get(inv);
		if(s == null) return new LazySet("empty stmt to invocation");
		return s;
	}
	
}
