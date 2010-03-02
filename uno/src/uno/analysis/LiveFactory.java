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
import java.util.Map;
import java.util.*;

import soot.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import uno.*;
import uno.toolkit.*;

public class LiveFactory {
	
	private static Map graphToLive = new HashMap();
	public static SimpleLiveLocals getInstance(UnitGraph g){
		SimpleLiveLocals sll = new SimpleLiveLocals(g);
		SootMethod m = g.getBody().getMethod();

//		Msg.toFile(Uno.name + "_live/"+ m.getDeclaringClass()+"/",m.getSubSignature()+".live");
//		Msg.println(m + "\n");
//
//		for(Iterator i=g.iterator();i.hasNext();){
//			Object obj = i.next();
//			Msg.println(obj + " : ");
//			for(Iterator j=sll.getLiveLocalsAfter((Unit)obj).iterator();j.hasNext();){
//
//				Msg.println("        " + j.next());
//			}
//		}			
//		Msg.toStdout();
		return sll;
	}
	private static String rmspace(String s){
		String r = "";
		for(int i=0;i<s.length();i++)
			if(s.charAt(i)==' ')
				r += '_';
			else
				r += s.charAt(i);
		return r;
	}
		
}
