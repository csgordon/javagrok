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

package uno;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import uno.analysis.PointsToAnalysis;
import uno.analysis.PointsToAnalysisFactory;

public class CallGraph {

	private static Map callersOf;
	private static Map superthisCallersOf;

	public static Set getSuperthisCallersOf(SootMethod method){
		if(!superthisCallersOf.containsKey(method))
			superthisCallersOf.put(method,new HashSet());

		return (Set) superthisCallersOf.get(method);
	}
	public static boolean isConstructor(SootMethod method){
		return method.getName().equals("<init>");
	}
	public static boolean hasCallers(SootMethod method){
		return !getCallersOf(method).isEmpty() || !getSuperthisCallersOf(method).isEmpty();
	}
	public static Set getCallersOf(SootMethod method) {
		// Change 0819: callers include callers of method's parents
		if(!callersOf.containsKey(method))
			callersOf.put(method,new HashSet());
		Set callers = (Set) callersOf.get(method);
		for(Iterator i= Hierarchy.getSuperMethodOf(method).iterator();i.hasNext();){
			callers.addAll(getCallersOf((SootMethod)i.next()));
		}

		return callers;
	}
	public static Set getCallees(){ return callersOf.keySet(); }

	public static int init(Set baseList, List list) {
		// TODO Auto-generated method stub
		//System.out.println("Building callgraph ");
		//int count = 0;
		
		callersOf = new HashMap();
		superthisCallersOf = new HashMap();
		
		int max_local = 0;	
		
		List classList = new LinkedList();
		for(Iterator i=Scene.v().getClasses().iterator();i.hasNext();){
			SootClass c = (SootClass) i.next();
			boolean pass = false;
			if(baseList.isEmpty()) pass = true;
			else for(Iterator j=baseList.iterator();j.hasNext();)
				if(c.getName().startsWith((String) j.next())) pass = true;
			// uncomment the following to allow incomplete callgraph
			if(!pass)continue;
			if(c.isPhantomClass())continue;
			classList.add(c);
		}
		int size = classList.size();
		int count = 0;
		
		for(Iterator i=classList.iterator();i.hasNext();){
			SootClass c = (SootClass) i.next();
			count++;
			System.out.print("1: (" +count + "/" +size+ ") " + c + " ... ");
			
			for(Iterator j=c.getMethods().iterator();j.hasNext();){
				SootMethod m = (SootMethod) j.next();
			
		//		System.out.println("Analysing " + m + " ");
				
				PointsToAnalysis p = PointsToAnalysisFactory.getInstance(m);
				int nlocal = p.getLocalCount();
				if(nlocal>max_local) max_local = nlocal;
				for(Iterator k=p.getCallees().iterator();k.hasNext();){
					SootMethod callee = (SootMethod) k.next();
		//			System.out.println("     has callee " + callee);
					getCallersOf(callee).add(m);
			
				}
				SootMethod superthisCallee = p.get_superORthis();
				getSuperthisCallersOf(superthisCallee).add(m);
		//		System.out.println(" (OK)");
				
			}
			System.out.println();
		}
		return max_local;
	}

}
