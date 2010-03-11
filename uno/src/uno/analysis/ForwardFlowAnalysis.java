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

/**
 * 
 */
package uno.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;


/**
 * @author kkma
 *
 */
public abstract class ForwardFlowAnalysis {

	protected ExceptionalUnitGraph g;
	private Map flowAfter;
		
	boolean debug = false;
	
	public ForwardFlowAnalysis(ExceptionalUnitGraph g) {
		this.g = g;
		this.flowAfter = new HashMap();
	}
	
	protected void doAnalysis() {
		List worklist = new LinkedList();
		for(Iterator i=g.getBody().getUnits().iterator();i.hasNext();) {
			Stmt stmt = (Stmt) i.next();
			flowAfter.put(stmt,initialSet());
			worklist.add(stmt);
		}
		while(!worklist.isEmpty()) {
			Stmt stmt = (Stmt) worklist.remove(0);
			List ins = new LinkedList();
			for(Iterator i=g.getPredsOf(stmt).iterator();i.hasNext();) {
				Stmt pred = (Stmt) i.next();
				Object predFlow = (Object) flowAfter.get(pred);
				ins.add(predFlow);
			}
			if(ins.size()==0) ins.add(initialSet());
			flowThrough(ins,stmt,flowAfter.get(stmt));
		}
 	}
	public Object getFlowAfter(Stmt stmt) {
		return (Object) flowAfter.get(stmt);
	}
	public abstract Object initialSet();
	public abstract void flowThrough(List obj_ins, Stmt stmt, Object obj_out);	
}
