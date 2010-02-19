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
import java.util.Map;
import java.util.Set;

import uno.toolkit.*;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;


public class Hierarchy {
	
	private static Map superOf;
	private static Map subOf;
	
	public static void init(){
		superOf = new HashMap();
		subOf = new HashMap();
		
		for(Iterator i=Scene.v().getClasses().iterator();i.hasNext();){
			SootClass c = (SootClass) i.next();
			Set s = new HashSet();
			try{
				if(!c.isInterface()){
					s.add(c.getSuperclass());
				}
			}catch(Exception e){}//Msg.println("Error1: "+e);}
			try{
				s.addAll(c.getInterfaces());
			}catch(Exception e){}//Msg.println("Error2: "+e);}
			for(Iterator j=s.iterator();j.hasNext();){
				SootClass superOfC = (SootClass) j.next();
				getSuperOf(c).add(superOfC);
				getSubOf(superOfC).add(c);
			}
		}		
	}

	public static Set getAllSubOf(SootClass c){
		if(!subOf.containsKey(c))
			subOf.put(c,new HashSet());
		Set a = new HashSet();
		Set b = new HashSet();
		a.addAll((Set)subOf.get(c));
		b.addAll(a);
		for(Iterator i=a.iterator();i.hasNext();){
			b.addAll(getAllSubOf((SootClass)i.next()));
		}
		return b;
	}
	public static Set getAllSuperOf(SootClass c){
		if(!superOf.containsKey(c))
			superOf.put(c,new HashSet());
		Set a = new HashSet();
		Set b = new HashSet();
		a.addAll((Set)superOf.get(c));
		b.addAll(a);
		for(Iterator i=a.iterator();i.hasNext();){
			b.addAll(getAllSuperOf((SootClass)i.next()));
		}
		return b;
	}

	public static Set getSuperOf(SootClass c){
		if(!superOf.containsKey(c))
			superOf.put(c,new HashSet());
		return (Set) superOf.get(c);
	}
	
	public static Set getSubOf(SootClass c){
		if(!subOf.containsKey(c))
			subOf.put(c,new HashSet());
		return (Set) subOf.get(c);
	}
	
	// FINE
	public static Set getSuperMethodOf(SootMethod m){
		Set r = new HashSet();
		if(m.getName().equals("<init>") || m.isStatic());
		else
		for(Iterator i=Hierarchy.getAllSuperOf(m.getDeclaringClass()).iterator();i.hasNext();){
			SootClass c = (SootClass) i.next();
			SootMethod sm = null;
			try{
				sm = c.getMethod(m.getSubSignature());
			}catch(Exception e){}
			if(sm!=null)r.add(sm);
		}
		return r;
	}
	
	
	// FINE
	public static Set getSubMethodOf(SootMethod m){
		Set r = new HashSet();
		if(m.getName().equals("<init>") || m.isStatic());
		else
		for(Iterator i=Hierarchy.getAllSubOf(m.getDeclaringClass()).iterator();i.hasNext();){
			SootClass c = (SootClass) i.next();
			SootMethod sm = null;
			try{
				sm = c.getMethod(m.getSubSignature());
			}catch(Exception e){}
			if(sm!=null)r.add(sm);
		}
		return r;
	}
}
