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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import uno.toolkit.Bool;

public class QueryFactory {

	private static Map queryMap = new HashMap();

	public static void reset(){
		queryMap = new HashMap();
		System.gc();
	}

	public static Collection getQueries(){ return queryMap.values(); }
	
	public static Query getStoreQuery(SootMethod method, int index) {
		// TODO Auto-generated method stub
		String key = "Store"+method+index;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new StoreQuery(method,index));
		return (Query) queryMap.get(key);
	}
	public static Query getFreshQuery(SootMethod method, int index) {
		return getFreshQuery(method,index,true);
	}
	public static Query getFreshQuery(SootMethod method, int index,boolean needed) {
		// TODO Auto-generated method stub
		String key = "Fresh"+method+index+needed;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new FreshQuery(method,index,needed));
		return (Query) queryMap.get(key);
	}
  
	public static Query getNotLeakParQuery(SootMethod method, int index) {		
		// TODO Auto-generated method stub
		String key = "NotLeakPar"+method+index;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new NotLeakParQuery(method,index));
		if(method.isNative()) return new FalseQuery((Query)queryMap.get(key));
		return (Query) queryMap.get(key);
	}
	public static Query getFreshInQuery(SootMethod method, int index, SootMethod caller) {
		return getFreshInQuery(method,index,caller,true);
	}
	public static Query getFreshInQuery(SootMethod method, int index, SootMethod caller,boolean b) {
		// TODO Auto-generated method stub
		String key = "FreshIn"+method+index+caller+b;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new FreshInQuery(method,index,caller,b));
		return (Query) queryMap.get(key);
	}
	public static Query getOwnQuery(SootMethod method,int index) {
		String key = "Own"+method+index;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new OwnQuery(method,index));
		return (Query) queryMap.get(key);	
	} 
	public static Query getOwnParQuery(SootMethod method,int index) {
		String key = "OwnPar"+method+index;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new OwnParQuery(method,index));
		return (Query) queryMap.get(key);	
	} 
	public static Query getUniqQuery(SootMethod method, int index) {
		return getUniqQuery(method,index,true);
	}
	public static Query getUniqQuery(SootMethod method, int index,boolean b) {
		String key = "Uniq"+method+index+b;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new UniqQuery(method,index,b));
		if(method.isNative()) return new FalseQuery((Query)queryMap.get(key));
		return (Query) queryMap.get(key);	
	}
	public static Query getUniqBaseQuery(SootMethod method) {
		String key = "UniqBase"+method;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new UniqBaseQuery(method));
		if(method.isNative()) return new FalseQuery((Query)queryMap.get(key));
		return (Query) queryMap.get(key);	
	}
	public static Query getOwnFieldQuery(SootField f) {
		String key = "OwnField"+f;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new OwnFieldQuery(f));
		return (Query) queryMap.get(key);
	}
	public static Query getOwnFieldInQuery(SootField f, SootMethod method) {
		String key = "OwnFieldIn"+f+method;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new OwnFieldInQuery(f,method));
		if(method.isNative()) return new FalseQuery((Query)queryMap.get(key));
		return (Query) queryMap.get(key);
	}
	public static Query getFFieldQuery(SootField f) {
		String key = "FField"+f;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new FFieldQuery(f));
		return (Query) queryMap.get(key);
	}
	public static Query getFFieldInQuery(SootField f, SootMethod method) {
		String key = "FFieldIn"+f+method;
		if(!queryMap.containsKey(key))
			queryMap.put(key,new FFieldInQuery(f,method));
		if(method.isNative()) return new FalseQuery((Query)queryMap.get(key));
		return (Query) queryMap.get(key);
	}

	/***/
	
	public static Query getStoreQuery(InvokeExpr inv, int index) {
		// TODO Auto-generated method stub
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getStoreQuery(method,index);
	}
	public static Query getFreshQuery(InvokeExpr inv, int index,boolean needed) {
		// TODO Auto-generated method stub
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getFreshQuery(method,index,needed);
	}  
	public static Query getFreshQuery(InvokeExpr inv, int index) {
		return getFreshQuery(inv,index,true);
	}  
	public static Query getNotLeakParQuery(InvokeExpr inv, int index) {
		// TODO Auto-generated method stub
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getNotLeakParQuery(method,index);
	}
	public static Query getFreshInQuery(InvokeExpr inv, int index, SootMethod caller) {
		// TODO Auto-generated method stub
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getFreshInQuery(method,index,caller);
	}
	public static Query getFreshInQuery(InvokeExpr inv, int index, SootMethod caller,boolean b) {
		// TODO Auto-generated method stub
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getFreshInQuery(method,index,caller,b);
	}
	public static Query getOwnQuery(InvokeExpr inv,int index) {
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getOwnQuery(method,index);
	} 
	public static Query getOwnParQuery(InvokeExpr inv,int index) {
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getOwnParQuery(method,index);
	} 
	//public static Query getOwnInQuery(InvokeExpr inv, int index, SootMethod caller) {
	//	SootMethod method = null;
	//	try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
	//	return getOwnInQuery(method,index,caller);
	//}
	public static Query getUniqQuery(InvokeExpr inv, int index) {
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getUniqQuery(method,index);
	}
	public static Query getUniqQuery(InvokeExpr inv, int index,boolean b) {
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getUniqQuery(method,index,b);
	}
	public static Query getUniqBaseQuery(InvokeExpr inv) {
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getUniqBaseQuery(method);
	}
	public static Query getFFieldInQuery(SootField f, InvokeExpr inv) {
		SootMethod method = null;
		try{ method = inv.getMethod(); }catch(Exception e){ return new FalseQuery(inv); }
		return getFFieldInQuery(f,method);
	}	
	
	//public static void init(Set exceptionList) {
	//	// TODO Auto-generated method stub
	//	// uniqueness of return
	//	for(Iterator i=exceptionList.iterator();i.hasNext();){
	//		String s = (String) i.next();
	//		String classname=null,methodname=null;
	//		for(int j=0;j<s.length();j++)
	//			if(s.charAt(j)==':'){
	//				classname = s.substring(0,j);
	//				methodname = s.substring(j+2,s.length());
	//				break;
	//			}
	//		if(classname==null)continue;	
	//		SootMethod method = Scene.v().getSootClass(classname).getMethod(methodname);
	//		String key = "Uniq"+method+"-1";

	//		Query q = new UniqQuery(method,-1);
	//		q.setTruth(Bool.TRUE);
	//		if(!queryMap.containsKey(key))
	//			queryMap.put(key,q);
	//	}
	//}


	

}
