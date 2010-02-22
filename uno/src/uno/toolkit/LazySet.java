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
 * Created on Mar 20, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package uno.toolkit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author kkma
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LazySet {

	public static int ID = 0;
	private static int max_size = 0;
	
	private int id;
	private Set actualContent;
	private boolean hasActualContent;
	private String name;
	private Set thisContent;
	private int color;

	private static int cur_color = 0;


	private static Set allLazySet = new HashSet();
	
	public LazySet(String name){
		color = cur_color;
		actualContent = null;
		hasActualContent = false;
		this.name = name;
		thisContent = new HashSet();
		id = ++ID;
		//allLazySet.add(this);
	}

	//public static void flattenAll(){
	//	for(Iterator i=allLazySet.iterator();i.hasNext();){
	//		LazySet ls = (LazySet)i.next();
	//		ls.flatten();
	//	}
	//}

	public void clear(){
		thisContent.clear();
	}
	
	public boolean add(Object o) {
		if(o==null) return false;
		return thisContent.add(o);
	}
	public boolean contains(Object o) {
		//System.out.println("contains");
		if(hasActualContent==false){
			flatten();
		}
		return actualContent.contains(o);
	}
	private void flatten() {
		actualContent = new HashSet();
		cur_color += 7;
		getContent(actualContent);
		
		hasActualContent = true;
		
		if(actualContent.size()>max_size){
			max_size = actualContent.size();
			//System.out.println("    ("+max_size+") from " + name);
		}
		
//		System.out.print(name + " : ");
//		for(Iterator i=this.iterator();i.hasNext();){
//			System.out.print(" | "+i.next());
//		}
//		System.out.println(" |");
	}
	
	private void getContent(Set contentBag){
		if(color==cur_color) return;
		color = cur_color;
		if(hasActualContent==true){
			contentBag.addAll(actualContent);
			return;
		}
		for(Iterator i=thisContent.iterator();i.hasNext();){
			Object obj = i.next();
			if(obj instanceof LazySet){
				((LazySet)obj).getContent(contentBag);
			}
			else{				
				contentBag.add(obj);
			}
		}	
	}

	public Iterator iterator() {
		//System.out.println("iterator");
		if(hasActualContent==false){
			flatten();
		}
		return actualContent.iterator();
	}
	public boolean addAll(LazySet c) {
		if(c==null) return false;
		// always flatten
		
		//thisContent.addAll(c.thisContent);
		//return true;
		
		return add(c);
	}
	//public String toString(){
	//	String s = "{"+id+"/"+name+": ";
	//	for(Iterator i=thisContent.iterator();i.hasNext();){
	//		Object obj = i.next();
	//		if(obj instanceof LazySet){
	//			s+="<"+((LazySet)obj).id+","+((LazySet)obj).name+">";
	//		}
	//		else
	//			s += obj.toString();
	//		if(i.hasNext()) s+=",";
	//	}
	//	
	//	return s + "}";
	//}
	public String toString(){
		String s = "{"+id+": ";
		for(Iterator i=thisContent.iterator();i.hasNext();){
			Object obj = i.next();
			if(obj instanceof LazySet){
				s+="<"+((LazySet)obj).id+">";
			}
			else
				s += obj.toString();
			if(i.hasNext()) s+=",";
		}
		
		return s + "}";
	}
}
