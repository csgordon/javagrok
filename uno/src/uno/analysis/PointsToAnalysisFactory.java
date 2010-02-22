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

import java.util.HashMap;
import java.util.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.*;
import soot.jimple.*;
import uno.toolkit.*;
import uno.*;

public class PointsToAnalysisFactory {
	private static Map methodToAnalysis = new HashMap();
	private static final boolean DEBUG = false;
	
	public static PointsToAnalysis getInstance(SootMethod method){
		if(!methodToAnalysis.containsKey(method)){
			LazySet.ID = 0;
			methodToAnalysis.put(method,new PointsToAnalysis(method));

			if(DEBUG){
			PointsToAnalysis pta = (PointsToAnalysis) methodToAnalysis.get(method);
			
			if(pta.isFailed()) return pta;

			SootMethod m = method;
			ExceptionalUnitGraph g = pta.getUnitGraph();
			
			Msg.toFile(Uno.name+"_pta/"+ m.getDeclaringClass()+"/",m.getSubSignature()+".pta");
			Msg.println(m + "\n");

			try{
			if(g!=null){
				Msg.println("Return:\t"+pta.getReturn());
				Msg.println("Bad:\t"+pta.getBad());
				for(int i=0;i<m.getParameterCount();i++)
					Msg.println("Parameter "+i+":\t"+pta.getPar(i));
				Msg.println("This:\t"+pta.getThis());
				for(Iterator i=m.getDeclaringClass().getFields().iterator(); i.hasNext();){
					SootField f = (SootField)i.next();
					Msg.println("Field "+f+":\t"+pta.getPointedByField(f));
				}
				Msg.println("Callees:");
				for(Iterator i=pta.getCallees().iterator();i.hasNext();){
					SootMethod callee = (SootMethod) i.next();
					Msg.println("        "+callee);
					Msg.println("        "+"Return:\t"+pta.getCalleeReturn(callee));
					for(int j=0;j<callee.getParameterCount();j++)
						Msg.println("        "+"Par"+j+":\t"+pta.getCalleePar(callee,j));
					Msg.println("        "+"LiveAfter:\t"+pta.getCalleeLiveAfter(callee));
				}
				Msg.println("Invocations:");
				for(Iterator i=pta.getInvocations().iterator();i.hasNext();){
					InvokeExpr inv  = (InvokeExpr) i.next();
					Msg.println("        "+inv);
					Msg.println("        "+"Base:\t"+pta.getInvocationBase(inv));
					Msg.println("        "+"Return:\t"+pta.getInvocationReturn(inv));
					for(int j=0;j<inv.getMethod().getParameterCount();j++)
						Msg.println("        "+"Par"+j+":\t"+pta.getInvocationPar(inv,j));
					Msg.println("        "+"LiveAfter:\t"+pta.getInvocationLiveAfter(inv));
				}
				Msg.println("BadFields:\t"+pta.getBadFields());
				Msg.println("SuperOrThis:\t"+pta.get_superORthis());
				SootMethod st = pta.get_superORthis();
				if(st!=null){
					for(int j=0;j<st.getParameterCount(); j++)
						Msg.println("SuperOrThis par"+j+ ":\t"+pta.get_superORthis_par_TO_obj(st,j));
				}
				else
					Msg.println("SuperOrThis is null.");
			}
			else{
				Msg.println("It is an abstract method without body.");
			}
			}catch(Exception e){
				Msg.println(e);
			}
			Msg.toStdout();
			}
		}

		PointsToAnalysis pta = (PointsToAnalysis) methodToAnalysis.get(method);
		return pta;
	}
}
