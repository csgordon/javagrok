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

import soot.*;
import java.util.*;


public class SVBTracker {

	private static Map clsToNonSVB = new HashMap();
	private static Map clsToSize = new HashMap();
	private static Set classList;

	public static void init(Set iClassList){
		classList = iClassList;
	}

	public static void report(SootField field){
		SootClass c = field.getDeclaringClass();
		// only class of the package is considered.
		if(!classList.contains(c.toString())) return;

		if(!clsToNonSVB.containsKey(c)){
			Set s = new HashSet();
			// all private ref-like fields
			for(Iterator i=c.getFields().iterator();i.hasNext();){
				SootField f = (SootField)i.next();
				if(f.isPrivate() && f.getType() instanceof RefLikeType)
					s.add(f);
			}
			clsToNonSVB.put(c,s);
			clsToSize.put(c,new Integer(s.size()));
		}
		((Set)clsToNonSVB.get(c)).remove(field);
	}

	public static int getAll(){
		int allsize = 0;
		
		for(Iterator i=clsToNonSVB.keySet().iterator();i.hasNext();){
			SootClass c = (SootClass)i.next();
			allsize += ((Integer)clsToSize.get(c)).intValue();
		}
		return allsize;
	}
	public static int getNSVB(){
		int nsvbsize = 0;
		
		for(Iterator i=clsToNonSVB.keySet().iterator();i.hasNext();){
			SootClass c = (SootClass)i.next();
			nsvbsize += ((Set)clsToNonSVB.get(c)).size();
		}
		return nsvbsize;
	}
	public static double ratio(){
		int allsize = 0;
		int nsvbsize = 0;
		
		for(Iterator i=clsToNonSVB.keySet().iterator();i.hasNext();){
			SootClass c = (SootClass)i.next();
			allsize += ((Integer)clsToSize.get(c)).intValue();
			nsvbsize += ((Set)clsToNonSVB.get(c)).size();
		}
		if(allsize==0) return 0;
		return nsvbsize/((double)allsize);
	}
}
