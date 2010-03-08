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

import uno.Uno;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.*;

import soot.*;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.jimple.NewArrayExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import uno.toolkit.LazySet;
import uno.toolkit.Msg;
import uno.Hierarchy;

/*
 * TODO: 1. passing this as argument (DONE) 2. static visibility (other.field)
 * (DONE) 3. array (DONE) 4. return l.f 5. in super(par), ignore the fact of
 * "passing par to super"
 */
public class PointsToFlowAnalysis extends ForwardFlowAnalysis {
	
	static Local sootRET;
	
	static Local sootVOID;
	//private HashMap stmtcounter;
	static {
		sootRET = new JimpleLocal("@RET", RefType.v());
		sootVOID = new JimpleLocal("@VOID", RefType.v());
	}
	
	public LazySet badFields;
	
	private JimpleBody body;
	
	public Set callees;
	
	public Map calleeToProperty; //*
	
	public Map fieldToSet; //*
	
	public Set invocations;
	
	public Map invocationToProperty; //*
	
	public Map invToInvocationsBefore; //***
	
	public PropertySets myPropertySets; //*
	
	private SimpleLiveLocals sll;
	
	private Map stmtToInvocationsBefore; //***
	
	public SootMethod superORthis = null;
	
	public Map superORthis_par_TO_obj; //*
	
	public LazySet thisSet;

	private Value[] parToR;
	
	public PointsToFlowAnalysis(ExceptionalUnitGraph g) {
		super(g);
		
		//	this.stmtcounter = new HashMap();
		this.body = (JimpleBody) g.getBody();
		
		callees = new HashSet();
		invocations = new HashSet();
		calleeToProperty = new HashMap();
		invocationToProperty = new HashMap();
		fieldToSet = new HashMap();
		thisSet = new LazySet("ThisSet");
		badFields = new LazySet("badfields");
		// no access modifer, public, protected are all bad...

		
		myPropertySets = new PropertySets("this",g.getBody().getMethod()
				.getParameterCount());
		sll = LiveFactory.getInstance(g);
		superORthis_par_TO_obj = new HashMap();
		stmtToInvocationsBefore = new HashMap();
		invToInvocationsBefore = new HashMap();

		parToR = new Value[body.getMethod().getParameterCount()];
		
		// restart the PointsToSet static content
		PointsToSet.init();

		// for each field, add an object to its PointsTo set: the initial value
		for (Iterator i = body.getMethod().getDeclaringClass().getFields()
				.iterator(); i.hasNext();) {
			SootField f = (SootField) i.next();
			this.getFieldSet(f).add(
					new JimpleLocal(f.getSignature() + " init value", RefType
							.v()));
			// no access modifer, public, protected are all bad...
			if(!f.isPrivate())
				badFields.add(f);
		}
		// for each parameter, record it down
		for (Iterator i = body.getUnits().iterator(); i.hasNext();) {
			Stmt stmt = (Stmt) i.next();
			if (stmt instanceof DefinitionStmt) {
				Value left = ((DefinitionStmt) stmt).getLeftOp();
				Value right = ((DefinitionStmt) stmt).getRightOp();
				if (right instanceof ParameterRef) {
					ParameterRef p = (ParameterRef) right;
					this.getMyPropertySets().getPAR(p.getIndex()).add(p);
					parToR[p.getIndex()] = left;
				}
				// usually "this" is treated as non-fresh except in constructors
				else if (right instanceof ThisRef){
						//&& !body.getMethod().getName().equals("<init>")) {
					this.thisSet.add(right);
				}
			} else
				break;
		}
		// at least one object in bad set
		this.getMyPropertySets().getBAD().add(
				new JimpleLocal("@BAD", RefType.v()));
		
		super.doAnalysis();
		//Msg.println(this);
		

		// output

		SootMethod m = g.getBody().getMethod();
		if(false && m!=null && m.isConcrete()){

		Msg.toFile(Uno.name+"_ptfa/"+ m.getDeclaringClass()+"/",m.getSubSignature()+".ptfa");
		Msg.println(m + "\n");

		for(Iterator i=g.getBody().getUnits().iterator();i.hasNext();){
			Stmt stmt = (Stmt)i.next();
			PointsToSet pts = (PointsToSet) getFlowAfter(stmt);

			Msg.println(stmt);
			Msg.println(" *  Preds of:");
			for(Iterator j=g.getPredsOf(stmt).iterator();j.hasNext();){
				Msg.println("        "+ j.next());
			}
			Msg.println(" *  PointsTo:");
			Msg.print(pts);
		}

		Msg.toStdout();
		}
	}
	
	private InvokeExpr getInvokeExpr(Stmt stmt){
		// return null if stmt is super()/this() call
		// specialinvoke r0.<java.lang.Object: void <init>()>();
		if(stmt instanceof InvokeStmt){
			InvokeExpr inv = ((InvokeStmt)stmt).getInvokeExpr();

			if(inv instanceof SpecialInvokeExpr){
				SpecialInvokeExpr sinv = (SpecialInvokeExpr) inv;
				if(!body.getMethod().isStatic() && sinv.getBase().toString().equals("r0"))
					return null;
			}
			return inv;
		}
		return null;
	}
	
	public void flowThrough(List obj_ins, Stmt stmt, Object obj_out) {
		
		PointsToSet out=(PointsToSet) obj_out;

		if(g.getPredsOf(stmt).size()==1)
		{
			Stmt pred = (Stmt)g.getPredsOf(stmt).get(0);
			if(getInvokeExpr(pred)!=null)
			{
				getInvocationsBefore(stmt).addAll(this.getInvocationsBefore(pred));
				getInvocationsBefore(stmt).add(getInvokeExpr(pred));
			}
			else 
				stmtToInvocationsBefore.put(stmt,getInvocationsBefore(pred));

			// 	LazySet lazy = new LazySet("stmtToInvocationsBefore");
			// 	lazy.addAll(getInvocationsBefore(stmt));
			// 	lazy.add(inv);
			// 	stmtToInvocationsBefore.put(stmt,lazy);
			// 	this.invToInvocationsBefore.put(inv,getInvocationsBefore(stmt));
		}
		else
		for(Iterator i=g.getPredsOf(stmt).iterator();i.hasNext();){
			Stmt pred = (Stmt) i.next();
			this.getInvocationsBefore(stmt).addAll(this.getInvocationsBefore(pred));
			if(getInvokeExpr(pred)!=null)
				getInvocationsBefore(stmt).add(getInvokeExpr(pred));
		}

		if(getInvokeExpr(stmt)!=null){
			invToInvocationsBefore.put(getInvokeExpr(stmt),getInvocationsBefore(stmt));
		}
		
		for(Iterator itr=obj_ins.iterator();itr.hasNext();)
		{
			PointsToSet in=(PointsToSet)itr.next();
			out.copy(in);
		}
		for(Iterator itr=obj_ins.iterator();itr.hasNext();)
			try{
				
				PointsToSet in=(PointsToSet)itr.next();
				
				
				//		if(!stmtcounter.containsKey(stmt))
				//			stmtcounter.put(stmt,new Integer(0));
				//		int cvalue = ((Integer)stmtcounter.get(stmt)).intValue()+1;
				//		stmtcounter.put(stmt,new Integer(cvalue));
				//		Msg.print(cvalue+ " : " + body.getMethod().getName() + ": " +stmt + " ... " +
				// in.size() +"\n");
				//		Msg.println(in.content());
				
				
				Value[] value = new Value[2];
				
				if (stmt instanceof DefinitionStmt) {
					DefinitionStmt s = (DefinitionStmt) stmt;
					value[0] = s.getLeftOp();
					value[1] = s.getRightOp();
					if(value[1] instanceof CastExpr)
						value[1] = ((CastExpr)value[1]).getOp();
				}
				else if (stmt instanceof InvokeStmt) {
					InvokeStmt s = (InvokeStmt) stmt;
					value[0] = sootVOID;
					value[1] = s.getInvokeExpr();
				}
				else if (stmt instanceof ReturnStmt) {
					ReturnStmt s = (ReturnStmt) stmt;
					value[0] = sootRET;
					value[1] = s.getOp();
				}
				else if (stmt instanceof ReturnVoidStmt) {
					value[0] = sootRET;
					value[1] = sootVOID;
				}
				else if (stmt instanceof ThrowStmt) {
					ThrowStmt s = (ThrowStmt) stmt;
					value[0] = sootRET;
					value[1] = s.getOp();
				}
				else {
					return;
				}
				
				
				//if(value[0].getType() instanceof PrimType) {
				//	return;
				//}
				
				// end of phase 1
				
				
				if(value[0]==sootRET){
					// TODO: is the following necessary?
					if(value[1] instanceof InstanceFieldRef){
						//assert(value[0] instanceof Local);
						InstanceFieldRef f = (InstanceFieldRef) value[1];
						if(!g.getBody().getMethod().isStatic() && f.getBase().toString().equals("r0")){
							
							this.getMyPropertySets().getRET().addAll(getFieldSet(f.getField()));
						}
						else
							this.getMyPropertySets().getRET().addAll(myPropertySets.getBAD());
					}
					else{
						//System.out.println("
						// this.getMyPropertySets().getRET().addAll("+value[1]+";");
						//System.out.println("before:
						// "+this.getMyPropertySets().getRET());
						this.getMyPropertySets().getRET().addAll(in.getEntitiesPointedBy(value[1]));
						//System.out.println("after: "
						// +this.getMyPropertySets().getRET());
					}
					
				}
				else if(value[1] instanceof ParameterRef){
					out.add(value[0],value[1]);
				}
				else if(value[1] instanceof ThisRef){
					out.add(value[0],value[1]);
				}
				else if(value[1] instanceof InvokeExpr){
					InvokeExpr inv = (InvokeExpr) value[1];
					boolean inv_is_phantom = false;
					try{
					    inv.getMethod();
					}catch(Exception e){
						// the method comes from a phantom class!
						inv_is_phantom = true;
					}
					
					
					SootMethod thisMethod = body.getMethod();
					SootMethod callee = null;
					if(!inv_is_phantom) callee = inv.getMethod();
					
					// super or this
					if(!inv_is_phantom && thisMethod.getName().equals("<init>") && callee.getName().equals("<init>") && 
						(Hierarchy.getSuperOf(thisMethod.getDeclaringClass()).contains(callee.getDeclaringClass()) ||
						thisMethod.getDeclaringClass().equals(callee.getDeclaringClass()))
						&& ((InstanceInvokeExpr)inv).getBase().toString().equals("r0")){
						superORthis = callee;
						for(int index=callee.getParameterCount()-1;index>=0;index--){
							this.get_superORthis_par_TO_obj(callee,index).addAll(out.getEntitiesPointedBy(inv.getArg(index)));
						}
					}
					else
					{
						this.invocations.add(inv);
						if(!inv_is_phantom) this.callees.add(inv.getMethod());
					}

					if(true) 
					{
						// 0319
						//LazySet lazy = new LazySet("stmtToInvocationsBefore");
						//lazy.addAll(getInvocationsBefore(stmt));
						//lazy.add(inv);
						//stmtToInvocationsBefore.put(stmt,lazy);
						//this.invToInvocationsBefore.put(inv,getInvocationsBefore(stmt));
						
						//stmtToInvocation.put(stmt,inv);
						
						if (inv instanceof InstanceInvokeExpr) {
							InstanceInvokeExpr iinv = (InstanceInvokeExpr) inv;
							this.getPropertySets(inv).getBase().addAll(
									in.getEntitiesPointedBy(iinv.getBase()));
						}
						if (value[1] instanceof SpecialInvokeExpr && value[0] == sootVOID) {
							
								SpecialInvokeExpr sinv = (SpecialInvokeExpr) inv;
								out.killAdd(sinv.getBase(), sinv);
							
						} else if (value[0] instanceof InstanceFieldRef) {
							InstanceFieldRef f = (InstanceFieldRef) value[0];
							if (!g.getBody().getMethod().isStatic()
									&& f.getBase().toString().equals("r0"))
								getFieldSet(f.getField()).add(inv);
							else
								myPropertySets.getBAD().add(inv);
						} else if (value[0] instanceof Local && value[0] != sootVOID) {
							out.killAdd(value[0], inv);
						}
						else
							myPropertySets.getBAD().add(inv);
						
						for (int i = inv.getArgCount() - 1; i >= 0; i--) {
							this.getPropertySets(inv).getPAR(i).addAll(
									in.getEntitiesPointedBy(inv.getArg(i)));
							if(inv_is_phantom)
								myPropertySets.getBAD().addAll(
									in.getEntitiesPointedBy(inv.getArg(i)));
							else
								this.getPropertySets(inv.getMethod()).getPAR(i).addAll(
									in.getEntitiesPointedBy(inv.getArg(i)));
						}
					}//end else
					if(inv_is_phantom)
						myPropertySets.getBAD().add(inv);
					this.getPropertySets(inv).getRET().add(inv);
					if(!inv_is_phantom) this.getPropertySets(inv.getMethod()).getRET().add(inv);
					// TODO: find the set of live objects after the invocation
					for (Iterator i = sll.getLiveLocalsAfter(stmt).iterator(); i
					.hasNext();) {
						Object ptr = i.next();
						this.getPropertySets(inv).getBAD().addAll(
								out.getEntitiesPointedBy(ptr));
						if(!inv_is_phantom)
						this.getPropertySets(inv.getMethod()).getBAD().addAll(
								out.getEntitiesPointedBy(ptr));
					}
					
				}
				// array exists in following constructs:
				// r1 = newarray
				// r1[?] = r2
				// r2 = r1[?]
				// where r1,r2 are locals
				
				// r1[?] = r2
				else{

				//if(value[0].getType() instanceof ArrayType ||
				//		value[1].getType() instanceof ArrayType){
				//	if(value[0] instanceof ArrayRef) value[0] = ((ArrayRef)value[0]).getBase();
				//	if(value[1] instanceof ArrayRef) value[1] = ((ArrayRef)value[1]).getBase();
				//	out.addAll(value[0],in.getEntitiesPointedBy(value[1]));
				//	out.addAll(value[1],in.getEntitiesPointedBy(value[0]));
				//}

				if(value[0] instanceof ArrayRef /* then value[1] instanceof Local */){
					ArrayRef array = (ArrayRef) value[0];
					this.getMyPropertySets().getBAD().addAll(in.getEntitiesPointedBy(value[1]));

					//out.addAll(array.getBase(),in.getEntitiesPointedBy(value[1]));
					//// if r1 is some parameter that is array
					//// then put entities to par also
					//for(int i=0;i<parToR.length;i++)
					//	if(array.getBase().equals(parToR[i]) && body.getMethod().getParameterType(i) instanceof ArrayType)
					//	{
					//		this.getMyPropertySets().getPAR(i).addAll(in.getEntitiesPointedBy(value[1]));
					//		break;
					//	}
				}
				// r2 = r1[?]
				else if(value[1] instanceof ArrayRef /* then value[0] instanceof Local */){
					ArrayRef array = (ArrayRef) value[1];
					out.killAddAll(value[0],this.getMyPropertySets().getBAD());
				}
				else if(value[1] instanceof NewArrayExpr){
					out.add(value[0],value[1]);
					this.getMyPropertySets().getBAD().add(value[1]);
				}
				else if(value[1] instanceof StaticFieldRef){ /* then value[0] instanceof Local */
					out.killAddAll(value[0],myPropertySets.getBAD());
				}
				// ? = r1
				else if(value[1] instanceof Local){
					if(value[0] instanceof Local)
						out.killAddAll(value[0],in.getEntitiesPointedBy(value[1]));
					else if(value[0] instanceof InstanceFieldRef){
						InstanceFieldRef f = (InstanceFieldRef) value[0];
						// my field
						if(!g.getBody().getMethod().isStatic() && f.getBase().toString().equals("r0")
								&& f.getField().getDeclaringClass().equals(g.getBody().getMethod().getDeclaringClass()))
							getFieldSet(f.getField()).addAll(in.getEntitiesPointedBy(value[1]));
						else{
							// TODO: my species' field
							// other.field = local
							
							/*
							 * class Subject{ private Object[] array; public void
							 * fun(Subject other){ Object[] tmparray = other.array;
							 */
							if(f.getBase().getType() instanceof RefType &&
									((RefType)f.getBase().getType()).getSootClass().equals(body.getMethod().getDeclaringClass()))
							{
								// static visibility error
								this.badFields.add(f.getField());
								SVBTracker.report(f.getField());
							}
							myPropertySets.getBAD().addAll(in.getEntitiesPointedBy(value[1]));
						}
					}
					else
						myPropertySets.getBAD().addAll(in.getEntitiesPointedBy(value[1]));
				}
				// r = l.f
				else if(value[1] instanceof InstanceFieldRef){
					//assert(value[0] instanceof Local);
					InstanceFieldRef f = (InstanceFieldRef) value[1];
					if(!g.getBody().getMethod().isStatic() && f.getBase().toString().equals("r0")
								&& f.getField().getDeclaringClass().equals(g.getBody().getMethod().getDeclaringClass()))
					{
						
						out.killAddAll(value[0],getFieldSet(f.getField()));
						//System.out.println(out.getEntitiesPointedBy(value[0]));
					}
					else{
						if(f.getBase().getType() instanceof RefType &&
								((RefType)f.getBase().getType()).getSootClass().equals(body.getMethod().getDeclaringClass()))
						{
							// static visibility error
							this.badFields.add(f.getField());
							SVBTracker.report(f.getField());
						}
						out.killAddAll(value[0],myPropertySets.getBAD());
					}
				
				}				
				// default
				else if(value[0] instanceof Local)
				{
				}
				}
				
			}
		catch(Exception e){
			System.out.println("Exception at "+stmt+" : ");
			e.printStackTrace();
		}
		//System.out.println(" >> " + out.toString());
	}	
	public SootMethod get_superORthis() {
		return this.superORthis;
	}
	
	public LazySet get_superORthis_par_TO_obj(SootMethod superORthis, int index) {
		String key = superORthis.toString() + index;
		if (!this.superORthis_par_TO_obj.containsKey(key)) {
			this.superORthis_par_TO_obj.put(key, new LazySet(
			"superORthis_par_TO_obj"));
		}
		return (LazySet) this.superORthis_par_TO_obj.get(key);
	}
	
	/**
	 * @return Returns the badFields.
	 */
	public LazySet getBadFields() {
		return badFields;
	}
	
	public Set getCallees() {
		// TODO Auto-generated method stub
		return this.callees;
	}
	
	public LazySet getFieldSet(SootField f) {
		if (!fieldToSet.containsKey(f))
			fieldToSet.put(f, new LazySet("Set pointed by field"));
		return (LazySet) fieldToSet.get(f);
	}
	
	public Set getInvocations() {
		// TODO Auto-generated method stub
		return this.invocations;
	}
	
	public LazySet getInvocationsBefore(Stmt stmt) {
		if (!this.stmtToInvocationsBefore.containsKey(stmt)) {
			this.stmtToInvocationsBefore.put(stmt, new LazySet(
			"stmtToInvocationsBefore"));
		}
		return (LazySet) this.stmtToInvocationsBefore.get(stmt);
	}
	
	public PropertySets getMyPropertySets() {
		// TODO Auto-generated method stub
		return this.myPropertySets;
	}
	
	public PropertySets getPropertySets(InvokeExpr inv) {
		if (!invocationToProperty.containsKey(inv))
			invocationToProperty.put(inv, new PropertySets(inv.toString(),inv.getArgCount()));
		return (PropertySets) invocationToProperty.get(inv);
	}
	
	public PropertySets getPropertySets(SootMethod method) {
		if (!calleeToProperty.containsKey(method))
			calleeToProperty.put(method, new PropertySets(method.toString(),method
					.getParameterCount()));
		return (PropertySets) calleeToProperty.get(method);
	}
	
	public LazySet getThisSet() {
		// TODO Auto-generated method stub
		return thisSet;
	}
	
	public Object initialSet() {
		// TODO Auto-generated method stub
		//return new PointsToSet();
		PointsToSet init = new PointsToSet();
		for(Iterator i=body.getLocals().iterator();i.hasNext();){
			Value v = (Value)i.next();
			char a = v.toString().charAt(0);
			if(a=='r')
				init.getEntitiesPointedBy(v);	
		}
		return init;
	}
	//public void merge(Object s, Object t) {
	//	// TODO Auto-generated method stub
	//	PointsToSet ps = (PointsToSet) s, pt = (PointsToSet) t;
	//	pt.union(ps);
	//}
	
	public String toString() {
		String s = body.getMethod() + "\n";
		for (Iterator i = body.getUnits().iterator(); i.hasNext();) {
			Stmt stmt = (Stmt) i.next();
			PointsToSet ps = (PointsToSet) this.getFlowAfter(stmt);
			s += "    " + stmt + "\n";
			s += ps;
		}
		//		s += " Return:\n";
		//		for(Iterator
		// i=this.getMyPropertySets().getRET().iterator();i.hasNext();){
		//			s += " " + (Value)i.next() + "\n";
		//		}
		//		s += " Bad:\n";
		//		for(Iterator
		// i=this.getMyPropertySets().getBAD().iterator();i.hasNext();){
		//			s += " " + (Value)i.next() + "\n";
		//		}
		return s;
	}
}
