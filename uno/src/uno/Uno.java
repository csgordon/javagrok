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

import java.io.*;
import java.util.*;

import soot.G;
import soot.PrimType;
import soot.RefType;
import soot.*;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import uno.analysis.*;
import uno.query.*;
import uno.query.Query;
import uno.query.QueryFactory;
import uno.toolkit.*;
import uno.toolkit.Timer;

public class Uno {

	/**
	 * @param args
	 */
	public static String name;
	public static int ID_LOOKING_AT;
	public static int RULE_LOOKING_AT;
	public static Set classList;


	// args
	public static String packageName;
	public static String inputFile;
	public static boolean printHierarchy =false;
	public static boolean printCallgraph =false;
	public static boolean printTrace =false;


	private static void parseOptions(String[] args){
		// Usage: java uno.Uno [options] <output dir> <input list>
		int len = args.length;
		if(len<2){
			System.out.println("Usage: java uno.Uno [options] <output dir> <input list>");
			System.exit(1);
		}
		packageName = args[len-2];
		inputFile = args[len-1];

		for(int i=0;i<len-2;i++){
			if(args[i].equals("-ph")){
				printHierarchy = true;
			}
			else if(args[i].equals("-pc")){
				printCallgraph = true;
			}
			else if(args[i].equals("-pt")){
				printTrace = true;
			}
			else {
				System.out.println("Usage: java uno.Uno [options] <output dir> <input list>");
				System.exit(1);
			}

		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Timer: 0 - Uno, 1 - Soot

		//MemoryStat.start();

		System.out.println("Uno - Inferring Aliasing and Encapsulation Properties for Java\n\n");
		Timer.reset();
		Timer.start(0);

		parseOptions(args);

		new File(packageName+"/").mkdir();
		name = packageName;


		G.v().out = new PrintStream(System.out){
			 public void println(String s) {
				 Msg.println("    Soot says: "+s);
			 }
		};

		Options.v().set_allow_phantom_refs(true);
		Options.v().set_full_resolver(true);
		
		Set baseList = new HashSet();
		classList = new HashSet();
		Set exceptionList = new HashSet();
		
		List list = new LinkedList();
		
		try{
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String str = null;
			while((str=in.readLine())!=null){
				if(str.equals("#")) break;
				baseList.add(str);
			}
			while((str=in.readLine())!=null){
				if(str.equals("#")) continue;
				classList.add(str);
				list.add(str);
			}
		}catch(Exception e){
			Msg.println("Error in loading input file.");
			return;
		}


		SVBTracker.init(classList);
		for(Iterator i=classList.iterator();i.hasNext();)
			Scene.v().addBasicClass((String)i.next());
		

	
		Timer.start(1);
		Msg.println("The time now is " + new Date());
		Msg.println();
		Msg.println("Running Soot version " + soot.Main.v().versionString);
		Scene.v().loadBasicClasses();
		Msg.println("Classes loaded.");
		Timer.finish(1);
		
		
		Set allMethods = new HashSet();
		Msg.println("# of classes: " + classList.size());
		int method_count = 0;
		for(Iterator i=classList.iterator();i.hasNext();){
			SootClass c = Scene.v().getSootClass((String) i.next());
			allMethods.addAll(c.getMethods());
			method_count += c.getMethodCount();
		}
		Msg.println("# of methods: " + method_count);
		Msg.println("");
		
		Msg.print("Generating hierarchy ...");
		Hierarchy.init();
		Msg.println("done");

		if(printHierarchy){
			Msg.println("Printing hierarchy ...");
			Msg.toFile(packageName + "/hierarchy");
			for(Iterator i=Scene.v().getClasses().iterator();i.hasNext();){
				SootClass c = (SootClass)i.next();
				for(Iterator k=c.getMethods().iterator();k.hasNext();){
					SootMethod m = (SootMethod)k.next();
					Msg.println(" * " + m + " has direct submethods:");
					for (Iterator j = Hierarchy.getSubMethodOf(m).iterator(); j.hasNext();) {
						SootMethod subm = (SootMethod)j.next();
						Msg.println("    " + subm);
					}
				}
			}			
		}
		Msg.toStdout();

		
		Msg.println("Generating call graph...");
		int maxlocal = CallGraph.init(baseList,list);
		Msg.println("Done.");

		Timer.finish(0); // don't count the time to write out file

		if(printCallgraph){
			Msg.println("Printing call graph...");
			Msg.toFile(packageName + "/callgraph");
			Set callees = allMethods;
			for (Iterator i = callees.iterator(); i.hasNext();) {
				SootMethod callee = (SootMethod) i.next();
				Msg.println(" * " + callee + " is called by");
				for (Iterator j = CallGraph.getCallersOf(callee).iterator(); j.hasNext();) {
					SootMethod caller = (SootMethod) j.next();
					Msg.println("        " + caller);
				}
			}			
		}
		Msg.toStdout();

		Msg.println("Analysis...");
		int size = classList.size();
		int count = 0;
		
		Timer.start(0);
		Msg.toFile(packageName+"/out");

		//Msg.println("Results ("+new Date()+") :\n");
		
		for(Iterator i=classList.iterator();i.hasNext();){
			SootClass c = Scene.v().getSootClass((String) i.next());
			System.out.println("2: ("+(++count)+"/"+size+") " + c);
			//Msg.println(c.toString() + "\n");

			Query q = null;

			for(Iterator j=c.getMethods().iterator();j.hasNext();){
				SootMethod m = (SootMethod) j.next();

				{
					q=QueryFactory.getUniqQuery(m,-1);
					q.getTruth(0);
					if(!printTrace){
						String text = q.toString();
						String annot = text.substring(0, text.indexOf(' '))+ " ";
						Msg.println("m " + annot + c.toString() + " " + m.getName() + " //" + q);
					}
				}
				if(!m.isStatic() && !CallGraph.isConstructor(m)){
					q=QueryFactory.getUniqBaseQuery(m);
					q.getTruth(0);
					if(!printTrace){Msg.println("*"+q);}
				}

				for(int k=0;k<m.getParameterCount();k++){
					// bypass primType arg
					if(m.getParameterType(k) instanceof PrimType) continue;
					// bypass java.lang.String
					if(m.getParameterType(k) instanceof RefType &&
						((RefType)m.getParameterType(k)).getClassName().equals("java.lang.String"))
						continue;
					
					if(!m.isStatic()){
		 			q=QueryFactory.getOwnQuery(m,k);
		 			q.getTruth(0);
					if(!printTrace){Msg.println("*"+q);}
					}
					if(!m.isStatic()){
		 			q=QueryFactory.getOwnParQuery(m,k);
		 			q.getTruth(0);
					if(!printTrace){Msg.println("*"+q);}
					}
					q=QueryFactory.getUniqQuery(m,k,true);
					q.getTruth(0);
					if(!printTrace){Msg.println("*"+q);}
					q=QueryFactory.getUniqQuery(m,k,false);
					q.getTruth(0);
					if(!printTrace){Msg.println("*"+q);}
					q=QueryFactory.getFreshQuery(m,k,true);
					q.getTruth(0);
					if(!printTrace){Msg.println("*"+q);}
					q=QueryFactory.getNotLeakParQuery(m,k);
					q.getTruth(0);
					if(!printTrace){Msg.println("*"+q);}
					if(!m.isStatic()){
					q=QueryFactory.getStoreQuery(m,k);
					q.getTruth(0);
					if(!printTrace){Msg.println("*"+q);}
					}
				}
			}
			for(Iterator j=c.getFields().iterator();j.hasNext();){
				SootField f = (SootField) j.next();

				Type type = f.getType();
				if(type instanceof PrimType) continue;
				if(type instanceof RefType &&
					((RefType)type).getClassName().equals("java.lang.String"))
					continue;
				
				q=QueryFactory.getFFieldQuery(f);
				q.getTruth(0);
				if(!printTrace){Msg.println("*"+q);}
				q=QueryFactory.getOwnFieldQuery(f);
				q.getTruth(0);
				if(!printTrace){Msg.println("*"+q);}
			}
			Msg.println("\n - - - - - - - - - - - \n");
		}
		Timer.finish(0);

		Msg.toStdout();
		Msg.println("Done.");
		//Msg.println("Done. Writing stat...");

		//try{
		//report(packageName+"/stat",true);
		//}catch(Exception e){ System.out.println(e);}

		//MemoryStat.stop();
	}

	public static boolean need(int splitnum,int target){
		return splitnum<0 || splitnum==target;
	}

	public static boolean nonTrivialReturn(SootMethod m){
		Type t = m.getReturnType();
		if(t instanceof PrimType) return false;
		if(t instanceof VoidType) return false;
		if(t instanceof RefType && ((RefType)t).getClassName().equals("java.lang.String")) return false;
		return true;
	}

	//                                  0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5
	public static final int[] scale =  {6,4,5,3,5,5,5,1,3,1,4,2,3}; // non local
	public static final int[] scale2 = {3,3,3,0,6,0,2,1,3,1,0,0,1}; // local

	public static void report(String filename,boolean NEED_IN_PKG){
		Msg.toFile(filename);
		Msg.println("TIME_SOOT\t" + Timer.get(1));
		Msg.println("TIME_UNO\t" + (Timer.get(0)-Timer.get(1)));
		Msg.println("");

		// count number of methods/parameters in package
		if(NEED_IN_PKG)
		{
			Msg.println("#PKG_CLASS\t" + classList.size());
			int num_method = 0;
			int num_cstr = 0;
			int num_called = 0;
			int num_called_cstr = 0;
			int num_method_par = 0;
			int num_cstr_par = 0;
			int num_called_par = 0;
			int num_called_cstr_par = 0;
			int num_field = 0;

			int num_uniqret_base = 0;
			int num_lentbase_base = 0;
			int num_own_base = 0;
			int num_own_called_base = 0;
			int num_own_called_cstr_base = 0;
			
			for(Iterator i=classList.iterator();i.hasNext();){
				SootClass c = Scene.v().getSootClass((String) i.next());
				for(Iterator j=c.getMethods().iterator();j.hasNext();){
					SootMethod m = (SootMethod) j.next();

					num_method++;
					if(CallGraph.isConstructor(m)) num_cstr++;
					if(CallGraph.hasCallers(m)) num_called++;
					if(CallGraph.isConstructor(m)&&CallGraph.hasCallers(m)) num_called_cstr++;
					if(!CallGraph.isConstructor(m) && nonTrivialReturn(m)) num_uniqret_base++;
					if(!CallGraph.isConstructor(m) && !m.isStatic()) num_lentbase_base++;
					
					

					for(int k=0;k<m.getParameterCount();k++){
						if(m.getParameterType(k) instanceof PrimType) continue;
						if(m.getParameterType(k) instanceof RefType &&
							((RefType)m.getParameterType(k)).getClassName().equals("java.lang.String"))
							continue;

						num_method_par++;
						num_own_base++;
						if(CallGraph.isConstructor(m)) {
							num_cstr_par++;
						}
						if(CallGraph.hasCallers(m)) {
							num_called_par++;
							if(!m.isStatic()) num_own_called_base++;
						}
						if(CallGraph.isConstructor(m)&&CallGraph.hasCallers(m)) {
							num_called_cstr_par++;
							if(!m.isStatic()) num_own_called_cstr_base++;
						}

					}
				}
				num_field += c.getFields().size();
			}
			Msg.println("#PKG_METHOD\t" + num_method);
			Msg.println("#PKG_METHOD_CSTR\t" + num_cstr);
			Msg.println("#PKG_METHOD_CALLED\t" + num_called);
			Msg.println("#PKG_METHOD_CALLED_CSTR\t" + num_called_cstr);
			Msg.println("#PKG_METHOD_PAR\t" + num_method_par);
			Msg.println("#PKG_METHOD_PAR_CSTR\t" + num_cstr_par);
			Msg.println("#PKG_METHOD_PAR_CALLED\t" + num_called_par);
			Msg.println("#PKG_METHOD_PAR_CALLED_CSTR\t" + num_called_cstr_par);
			Msg.println("#PKG_FIELD\t" + num_field);
			Msg.println("#num_uniqret_base\t" + num_uniqret_base);
			Msg.println("#num_lentbase_base\t" + num_lentbase_base);
			Msg.println("#num_own_base\t" + num_own_base);
			Msg.println("#num_own_called_base\t" + num_own_called_base);
			Msg.println("#num_own_called_cstr_base\t" + num_own_called_cstr_base);
			Msg.println("");
		}
		else
		// count number of methods/parameters in total
		{
			Msg.println("#TTL_CLASS\t" + Scene.v().getClasses().size());
			int num_method = 0;
			int num_cstr = 0;
			int num_called = 0;
			int num_called_cstr = 0;
			int num_method_par = 0;
			int num_cstr_par = 0;
			int num_called_par = 0;
			int num_called_cstr_par = 0;
			int num_field = 0;
			
			for(Iterator i=Scene.v().getClasses().iterator();i.hasNext();){
				SootClass c = (SootClass)i.next();
				for(Iterator j=c.getMethods().iterator();j.hasNext();){
					SootMethod m = (SootMethod) j.next();

					num_method++;
					if(CallGraph.isConstructor(m)) num_cstr++;
					if(CallGraph.hasCallers(m)) num_called++;
					if(CallGraph.isConstructor(m)&&CallGraph.hasCallers(m)) num_called_cstr++;

					for(int k=0;k<m.getParameterCount();k++){
						if(m.getParameterType(k) instanceof PrimType) continue;
						if(m.getParameterType(k) instanceof RefType &&
							((RefType)m.getParameterType(k)).getClassName().equals("java.lang.String"))
							continue;

						num_method_par++;
						if(CallGraph.isConstructor(m)) num_cstr_par++;
						if(CallGraph.hasCallers(m)) num_called_par++;
						if(CallGraph.isConstructor(m)&&CallGraph.hasCallers(m)) num_called_cstr_par++;
					}
				}
				num_field += c.getFields().size();
			}
			Msg.println("#TTL_METHOD\t" + num_method);
			Msg.println("#TTL_METHOD_CSTR\t" + num_cstr);
			Msg.println("#TTL_METHOD_CALLED\t" + num_called);
			Msg.println("#TTL_METHOD_CALLED_CSTR\t" + num_called_cstr);
			Msg.println("#TTL_METHOD_PAR\t" + num_method_par);
			Msg.println("#TTL_METHOD_PAR_CSTR\t" + num_cstr_par);
			Msg.println("#TTL_METHOD_PAR_CALLED\t" + num_called_par);
			Msg.println("#TTL_METHOD_PAR_CALLED_CSTR\t" + num_called_cstr_par);
			Msg.println("#TTL_FIELD\t" + num_field);
			Msg.println("");
		}


		String pred_name = "";
		int	pred_num = -1;

		//UNIQRET
		{
			pred_name = "UNIQRET";
			pred_num = 0;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof UniqQuery)) continue;
				if(!q.isQueried()) continue;
				UniqQuery query = (UniqQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(!query.isUniqRet()) continue;
				if(query.alwaysTrue()) continue;
				SootMethod m = query.getMethod();
				if(CallGraph.isConstructor(m) || !nonTrivialReturn(m)) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//LENTPAR
		{
			pred_name = "LENTPAR";
			pred_num = 1;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof UniqQuery)) continue;
				if(!q.isQueried()) continue;
				UniqQuery query = (UniqQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.isUniqRet()) continue;
				if(query.getStrict()==false)continue;
				if(query.alwaysTrue()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//LENTBASE
		{
			pred_name = "LENTBASE";
			pred_num = 2;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof UniqBaseQuery)) continue;
				if(!q.isQueried()) continue;
				UniqBaseQuery query = (UniqBaseQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.alwaysTrue()) continue;
				SootMethod m = query.getMethod();
				if(m.isStatic() || CallGraph.isConstructor(m)) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//UNIQPAR-true
		{
			pred_name = "UNIQPAR-true";
			pred_num = 3;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof FreshQuery)) continue;
				if(!q.isQueried()) continue;
				FreshQuery query = (FreshQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.getStrict()==false)continue;
				if(query.alwaysTrue()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//UNIQPAR-false
		{
			pred_name = "UNIQPAR-false";
			pred_num = 3;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof FreshQuery)) continue;
				if(!q.isQueried()) continue;
				FreshQuery query = (FreshQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.getStrict()==true)continue;
				if(query.alwaysTrue()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//UNIQPAR-IN
		{
			pred_name = "UNIQPAR-IN";
			pred_num = 4;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof FreshInQuery)) continue;
				if(!q.isQueried()) continue;
				FreshInQuery query = (FreshInQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.getStrict()==false)continue;
				if(query.alwaysTrue()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//OWN
		{
			pred_name = "OWN";
			pred_num = 5;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof OwnQuery)) continue;
				if(!q.isQueried()) continue;
				OwnQuery query = (OwnQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.alwaysTrue()) continue;
				if(query.getMethod().isStatic()) continue;


				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//NESCPAR
		{
			pred_name = "NESCPAR";
			pred_num = 6;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof NotLeakParQuery)) continue;
				if(!q.isQueried()) continue;
				NotLeakParQuery query = (NotLeakParQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.alwaysTrue()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//NESCFIELD
		{
			pred_name = "NESCFIELD";
			pred_num = 7;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof FFieldQuery)) continue;
				if(!q.isQueried()) continue;
				FFieldQuery query = (FFieldQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getField())) continue;
				if(query.alwaysTrue()) continue;
				SootField f = query.getField();
				Type type = f.getType();
				if(type instanceof PrimType) continue;
				if(type instanceof RefType &&
					((RefType)type).getClassName().equals("java.lang.String"))
					continue;
				if(f.isStatic()) continue;
				if(!f.isPrivate()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//STORE
		{
			pred_name = "STORE";
			pred_num = 9;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof StoreQuery)) continue;
				if(!q.isQueried()) continue;
				StoreQuery query = (StoreQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.alwaysTrue()) continue;
				if(query.getMethod().isStatic()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//OWNPAR
		{
			pred_name = "OWNPAR";
			pred_num = 10;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof OwnParQuery)) continue;
				if(!q.isQueried()) continue;
				OwnParQuery query = (OwnParQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.alwaysTrue()) continue;
				if(query.getMethod().isStatic()) continue;


				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
					if(CallGraph.hasCallers(query.getMethod())){
						tru_called++;
						if(CallGraph.isConstructor(query.getMethod()))
							tru_called_cstr++;
					}
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.println("#"+pred_name+"_TRUE_CALLED\t" + tru_called);
			Msg.println("#"+pred_name+"_TRUE_CALLED_CSTR\t" + tru_called_cstr);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}

		//OWNFIELD
		{
			pred_name = "OWNFIELD";
			pred_num = 11;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof OwnFieldQuery)) continue;
				if(!q.isQueried()) continue;
				OwnFieldQuery query = (OwnFieldQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getField())) continue;
				if(query.alwaysTrue()) continue;
				SootField f = query.getField();
				Type type = f.getType();
				if(type instanceof PrimType) continue;
				if(type instanceof RefType &&
					((RefType)type).getClassName().equals("java.lang.String"))
					continue;
				if(f.isStatic()) continue;
				if(!f.isPrivate()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		
		//OWNFIELD-IN
		{
			pred_name = "OWNFIELD-IN";
			pred_num = 12;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof OwnFieldInQuery)) continue;
				if(!q.isQueried()) continue;
				OwnFieldInQuery query = (OwnFieldInQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.alwaysTrue()) continue;
				SootField f = query.getField();
				Type type = f.getType();
				if(type instanceof PrimType) continue;
				if(type instanceof RefType &&
					((RefType)type).getClassName().equals("java.lang.String"))
					continue;
				if(f.isStatic()) continue;
				if(!f.isPrivate()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//NESCFIELD-IN
		{
			pred_name = "NESCFIELD-IN";
			pred_num = 8;

			int total = 0;
			int tru = 0;
			int tru_called = 0;
			int tru_called_cstr = 0;

			int[] local = new int[10];
			int[] nonlocal = new int[10];
			for(int i=0;i<10;i++) local[i]=nonlocal[i]=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof FFieldInQuery)) continue;
				if(!q.isQueried()) continue;
				FFieldInQuery query = (FFieldInQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(query.alwaysTrue()) continue;
				SootField f = query.getField();
				Type type = f.getType();
				if(type instanceof PrimType) continue;
				if(type instanceof RefType &&
					((RefType)type).getClassName().equals("java.lang.String"))
					continue;
				if(f.isStatic()) continue;
				if(!f.isPrivate()) continue;

				total++;
				for(int j=0;j<10;j++){
					if(j<scale2[pred_num]&&query.thisLocalIsFalse(j)) local[j]++;
					if(query.nonlocalRule[j]>0) nonlocal[j]++;
				}
				if(query.truth.val!=Bool.FALSE){
					tru++;
				}
			}
			Msg.println("#"+pred_name+"_TOTAL\t" + total);
			Msg.println("#"+pred_name+"_TRUE\t" + tru);
			Msg.print("#"+pred_name+"_LOCAL\t");
			for(int i=0;i<scale2[pred_num];i++)
				Msg.print(local[i]+"\t");
			Msg.println("");
			Msg.print("#"+pred_name+"_NONLOCAL\t");
			for(int i=0;i<scale[pred_num];i++)
				Msg.print(nonlocal[i]+"\t");
			Msg.println("");
			Msg.println("");
		}
		//static visibility badfields
		{
			Msg.println("#NSVBADFIELDS\t" + uno.analysis.SVBTracker.getNSVB());
			Msg.println("#ALLFIELDS\t" + uno.analysis.SVBTracker.getAll());
		}
		// constructors that returns unique
		{
			int num=0,num_true=0,num_called=0,num_called_true=0;
			for(Iterator i=QueryFactory.getQueries().iterator();i.hasNext();){
				Query q = (Query) i.next();
				if(!(q instanceof UniqQuery)) continue;
				if(!q.isQueried()) continue;
				UniqQuery query = (UniqQuery)q;
				if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;
				if(!query.isUniqRet()) continue;
				//if(query.alwaysTrue()) continue;
				SootMethod m = query.getMethod();
				//if(!nonTrivialReturn(m)) continue;
				if(!CallGraph.isConstructor(m)) continue;

				boolean called = CallGraph.hasCallers(m);

				num++;
				if(called) num_called++;
				
				if(query.truth.val!=Bool.FALSE){
					num_true++;
					if(called) num_called_true++;
				}
			}
			Msg.println("#CSTR_UNIQRET\t" + num);
			Msg.println("#CSTR_UNIQRET_TRUE\t" + num_true);
			Msg.println("#CSTR_UNIQRET_CALLED\t" + num_called);
			Msg.println("#CSTR_UNIQRET_CALLED_TRUE\t" + num_called_true);
		}
		Msg.toStdout();
	}

				//if(NEED_IN_PKG && !IN_PKG(query.getMethod())) continue;

	public static boolean IN_PKG(SootMethod m){
		return classList.contains(m.getDeclaringClass().toString());
	}
	public static boolean IN_PKG(SootField f){
		return classList.contains(f.getDeclaringClass().toString());
	}
}
