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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.RefLikeType;
import soot.*;
import soot.jimple.InvokeExpr;
import uno.toolkit.LazySet;


/**
 * @author kkma
 *
 */
public class PointsToSet {

	private Map refToEntities;
	private static Map arrayToEntities;
	
	public static void init(){
		arrayToEntities = new HashMap();
	}

	private static LazySet getEntitiesPointedBy_array(Object obj)
	{
		Value v = (Value) obj;
		if(v.getType() instanceof ArrayType){
			if(!arrayToEntities.containsKey(obj))
				arrayToEntities.put(obj,new LazySet(obj.toString()));
			return (LazySet) arrayToEntities.get(obj);
		}
		else return null;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
//	protected void finalize() throws Throwable {
//		// TODO Auto-generated method stub
//		System.out.println("Finalize PointsToSet");
//		super.finalize();
//	}
	public PointsToSet() {
		refToEntities = new HashMap();
	}


	public void add(Object ref, Object entity) {
		if(!(((Value)ref).getType() instanceof RefLikeType)) return;
		this.getEntitiesPointedBy(ref).add(entity);
	}
	
	public void addAll(Object r1,LazySet entities) {
		if(!(((Value)r1).getType() instanceof RefLikeType)) return;
		this.getEntitiesPointedBy(r1).addAll(entities);
	}
	
	public void killAdd(Object ref, Object entity) {
		if(!(((Value)ref).getType() instanceof RefLikeType)) return;
		getEntitiesPointedBy(ref).clear();
		getEntitiesPointedBy(ref).add(entity);
	}
	public void killAddAll(Object ref, LazySet entities) {
		if(!(((Value)ref).getType() instanceof RefLikeType)) return;
		getEntitiesPointedBy(ref).clear();
		getEntitiesPointedBy(ref).addAll(entities);
	}
	
	public LazySet getEntitiesPointedBy(Object r) {
		if(!(((Value)r).getType() instanceof RefLikeType)) 
			return new LazySet(r.toString()+" *ERROR*");
		Value v = (Value) r;
		if(v.getType() instanceof ArrayType)
			return getEntitiesPointedBy_array(v);
		else if(v.getType() instanceof NullType)
			return new LazySet("null");
		else if(!refToEntities.containsKey(r)) {
			refToEntities.put(r,new LazySet(r.toString()));
		}
		return (LazySet) refToEntities.get(r);
	}
//	public Set getObjectsPointingTo(Object entity) {
//		return null;
//	}
	

	//public void union(Object fin) {
	//	PointsToSet in = (PointsToSet) fin;
	//	for(Iterator i=in.refToEntities.keySet().iterator();i.hasNext();){
	//		Object ref = (Object) i.next();
	//		//System.out.print("<");
	//		this.getEntitiesPointedBy(ref).addAll(in.getEntitiesPointedBy(ref));
	//		//System.out.print("\b");
	//	}
	//	//System.out.println("");
	//}
	
	public String toString() {
		String r = "";
		for(Iterator i=this.refToEntities.keySet().iterator();i.hasNext();){
			Object key = i.next();
			r += "        " + key + "->\t" + this.refToEntities.get(key) + "\n";
		}
		return r;
	}
	public boolean equals(Object obj) {
//		PointsToSet s = (PointsToSet) obj;
//		return this.refToEntities.equals(s.refToEntities) /* && invocationssofar */;
		return false;
	}
	//public boolean equals(Object obj) {
	//	PointsToSet s = (PointsToSet) obj;
	//	System.out.println();	
	//	if(! this.refToEntities.equals(s.refToEntities) ){
	//	
	//		PointsToSet oldp = s;
	//		PointsToSet newp = this;
	//		
	//		for(Iterator i=newp.refToEntities.keySet().iterator();i.hasNext();){
	//			Object key = i.next();
	//			if(!oldp.refToEntities.containsKey(key)){
	//				System.out.println("    New ref: "+key);
	//			}
	//			else if(!((Set)newp.refToEntities.get(key)).equals(((Set)oldp.refToEntities.get(key)))){

	//				System.out.println("        New values of "+key+":");
	//				for(Iterator j=((Set)newp.refToEntities.get(key)).iterator();j.hasNext();){
	//					Object val = j.next();
	//					System.out.println("            "+val);
	//				}
	//				System.out.println("        Old values of "+key+":");
	//				for(Iterator j=((Set)oldp.refToEntities.get(key)).iterator();j.hasNext();){
	//					Object val = j.next();
	//					System.out.println("            "+val);
	//				}
	//			}
	//		}
	//		return false;
	//	}
	//	return true;
	//}
	public int size(){
		int s = 0;
		for(Iterator i=this.refToEntities.keySet().iterator();i.hasNext();){
			s += ((Set)this.refToEntities.get(i.next())).size();
		}
		return s;
	}

	
	// input is an old pointstoset
	public void diff(Object obj){
		
//		if(!(obj instanceof PointsToSet)) return;
//		PointsToSet oldp = (PointsToSet) obj;
//		PointsToSet newp = this;
//		
//		for(Iterator i=newp.refToEntities.keySet().iterator();i.hasNext();){
//			Object key = i.next();
//			if(!oldp.refToEntities.containsKey(key)){
//				//System.out.println("    New ref: "+key);
//			}
//			else if(!((Set)newp.refToEntities.get(key)).equals(((Set)oldp.refToEntities.get(key)))){
//
//				System.out.println("        New values of "+key+":");
//				for(Iterator j=((Set)newp.refToEntities.get(key)).iterator();j.hasNext();){
//					Object val = j.next();
//					System.out.println("            "+val);
//				}
//				System.out.println("        Old values of "+key+":");
//				for(Iterator j=((Set)oldp.refToEntities.get(key)).iterator();j.hasNext();){
//					Object val = j.next();
//					System.out.println("            "+val);
//				}
//			}
//		}

		
	}
	public String content(){
		// TODO: print the content!
		String s = "";
//		for(Iterator i=this.refToEntities.keySet().iterator();i.hasNext();){
//			Object key = i.next();
//			s+="  " + key + " -> ";
//			for(Iterator j=((Set)this.refToEntities.get(key)).iterator();j.hasNext();){
//				Object value = j.next();
//				s+= value + " ";
//			}
//			s += "\n";
//		}
//		
		return s;
	}


	/**
	 * @param in
	 */
	public void copy(PointsToSet in) {
		// TODO Auto-generated method stub
		for(Iterator i=in.refToEntities.keySet().iterator();i.hasNext();){
			Object key = i.next();
			this.getEntitiesPointedBy(key).addAll(in.getEntitiesPointedBy(key));
		}
	}
}
