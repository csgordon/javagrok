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

import java.util.*;

import soot.ArrayType;
import soot.PrimType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;
import uno.toolkit.Bool;
import uno.toolkit.LazySet;
import uno.toolkit.Msg;
import uno.Uno;

public abstract class Query {

	protected int ID;
	protected boolean isUniqReturn = false;

	protected String name;
	public Bool truth;
	private static Set simplifiedClasses = new TreeSet();

	private boolean isQueried;
	
	public int[] localRule = new int[10];
	public int[] nonlocalRule = new int[10];
	
	public Query(){
		isQueried = false;
		
		//truth = new Bool(Bool.NA);
		truth = new Bool(Bool.TRUE);
		queriedBy = new HashSet();
		uniqQueriedBy = new HashSet();

		for(int i=0;i<10;i++)
			localRule[i] = nonlocalRule[i] = 0;

	}
	
	public void setTruth(String b){truth.val = b;}
	
	/**
	 * @return Returns the simplifiedClasses.
	 */
	public static Set getSimplifiedClasses() {
		return simplifiedClasses;
	}

	protected abstract List[] getQueries();

	private Set queriedBy;
	public Set uniqQueriedBy;
	public int color = 0;
	
	private List[] Internal_getQueries(){
		List[] queries = getQueries();
		for(int index=0;index<queries.length;index++)
		for(Iterator i=queries[index].iterator();i.hasNext();){
			Query q = (Query) i.next();
			q.queriedBy.add(this);
			//if(q.isUniqReturn && this.isUniqReturn) q.uniqQueriedBy.add(this);
		}		
		return queries;
	}

	protected List[] createListArray(int size){
		List[] r = new List[size];
		for(int i=0;i<size;i++) r[i] = new LinkedList();
		return r;
	}
	
	public boolean rule_make_me_false = false ;

	public boolean isQueried(){ return isQueried; }

	public Bool getTruth(int level){
		
		if(isQueried) return truth;

		isQueried = true;

		if(this.alwaysTrue()){ // like prim/String parameter
			truth.val = Bool.TRUE;
		}
		else if(this.isLocallyFalse()){
			truth.val = Bool.FALSE;
		}
		else{
			//truth.val = Bool.TRUE; //initially true
			List[] queries = this.Internal_getQueries();

			boolean stop = false;
			for(int index=0;index<queries.length;index++)
			{
				for(Iterator i=queries[index].iterator();i.hasNext();){
					Query q = (Query) i.next();
					Bool result = q.getTruth(level+1);
					if(result.val==Bool.FALSE){
						// q makes THIS false
						truth.val = Bool.FALSE;
						nonlocalRule[index]++;
						stop = true;
						//break;
					} 
				}
				//if(stop) break;
			}
		}
		if(Uno.printTrace){
			String offset = ""; for(int k=0;k<level;k++) offset += "  ";
			if(level==0) Msg.print("*");
			Msg.println(offset+this);
		}

		if(truth.val ==(Bool.FALSE)) setFalse(level+1);
		return truth;
	}
	
	 
	/**
	 * 
	 */
	private void setFalse(int level) {
		String offset = ""; for(int k=0;k<level;k++) offset += "  ";
		for(Iterator i=this.queriedBy.iterator();i.hasNext();){
			Query q = (Query) i.next();
			if(q.truth.val!=Bool.FALSE){
				// THIS makes q false
				q.truth.val = Bool.FALSE;

				if(Uno.printTrace){
					Msg.println(offset + "Make " + q);
				}
				q.setFalse(level+1);
			}
		}		
	}

	protected abstract boolean isLocallyFalse();

	protected abstract void setName();

	public abstract boolean thisLocalIsFalse(int rule);

	public abstract boolean alwaysTrue();

	public String toString(){
		setName();
		return name + " : " + truth;
	}
	
	protected boolean share(LazySet s1,LazySet s2){
//		System.out.println("share?");
//		System.out.println("    " + s1);
//		System.out.println("    " + s2);
		for(Iterator i=s1.iterator();i.hasNext();){
			if(s2.contains(i.next())) return true;
		}
		return false;
	}
	
	private static final int length = 40;
	protected String simplify(SootClass c){
		return c.toString();
//		String s = c.getShortName() +" ";
//		for(int i=40-s.length();i>0;i--) s+=" ";
//		s+= c.getPackageName();
//		this.simplifiedClasses.add(s);
//		return c.getShortName();
	}
	protected String simplify(SootField f){
		return f.toString();
		//return simplify(f.getDeclaringClass()) + "::" + f.getName();
	}
	protected String simplify(SootMethod method){
		return method.toString();
//		String s=simplify(method.getDeclaringClass()) + "::";
//		//
//		{
//			boolean isArray = false;
//			Type t = method.getReturnType();
//			while (true) {
//				if (t instanceof ArrayType) {
//					t = ((ArrayType) t).getArrayElementType();
//					isArray = true;
//				} else if (t instanceof RefType) {
//					s += simplify(((RefType)t).getSootClass());
//					break;
//				} else if (t instanceof PrimType) {
//					s += "@Prim";
//					break;
//				} else if (t instanceof VoidType) {
//					s += "void";
//					break;
//				} else {
//					s += "@Unknown";
//					break;
//				}
//			}
//			if(isArray) s+= "[]";
//		}
//		s += " ";
//		
//		s += method.getName();
//		
//		s += "(";
//		
//		for(int i=0;i<method.getParameterCount();i++){
//			if(i>0)s+=",";
//			boolean isArray = false;
//			Type t = method.getParameterType(i);
//			while (true) {
//				if (t instanceof ArrayType) {
//					t = ((ArrayType) t).getArrayElementType();
//					isArray = true;
//				} else if (t instanceof RefType) {
//					s += simplify(((RefType)t).getSootClass());
//					break;
//				} else if (t instanceof PrimType) {
//					s += "@Prim";
//					break;
//				} else if (t instanceof VoidType) {
//					s += "void";
//					break;
//				} else {
//					s += "@Unknown";
//					break;
//				}
//			}
//			if(isArray) s+= "[]";
//		}
//		
//		s += ")";
//		
//		return s;
	}
	protected LazySet singleton(Object obj){
		LazySet r = new LazySet("Singleton containing "+obj);
		r.add(obj);
		return r;
	}
}
