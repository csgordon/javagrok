//
// $Id$

package org.javagrok.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.tools.Diagnostic;

import javax.lang.model.SourceVersion;
import javax.lang.model.util.Types;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.source.util.AbstractTypeProcessor;
import com.sun.source.util.TreePath;

import org.javagrok.analysis.AnalysisContext;
import org.javagrok.analysis.Analyzer;
import org.javagrok.analysis.ExceptionProperty;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.comp.Env;
import com.sun.source.tree.*;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.main.JavaCompiler;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * The main entry point for the javac analysis executing annotation processor.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ExceptionProcessor extends AbstractTypeProcessor
{
    private class ExceptionScanner extends TreeScanner {
	/* This is a class we SHOULD be using for throwstack,
	 * but we can't new() custom objects due to a compiler bug...
	private class ExceptionThrow {
	    public TypeMirror throwType;
	    public Stack<JCExpression> throwPath;
	    public Stack<Integer> throwSigns;
	    public boolean sometimes;
	}
	*/
	private final AnalysisContextImpl ctx;
	private List<String> exns;
	private List<String> cond_exns;
	private Stack<JCExpression> current_path;
	private Stack<Integer> current_signs;
	private Stack<JCClassDecl> current_class;
	private Stack<JCMethodDecl> current_method;
	private Types types;
	private Stack<HashSet<Object[]>> throwstack;
	private Stack<HashSet<Object[]>> appstack;
	private int untraceable_branches;
	private PrintWriter writer;

	private HashMap<JCMethodDecl,HashSet<Object[]>> throwmap;
	private HashMap<JCMethodDecl,HashSet<Object[]>> callmap;
	private HashMap<Symbol,JCMethodDecl> methdefs;
	private HashMap<JCMethodDecl,String> locmap;

	public ExceptionScanner(final AnalysisContextImpl ctx, String splatfile) throws IOException {
	    this.ctx = ctx;
	    current_class = new Stack<JCClassDecl>();
	    current_method = new Stack<JCMethodDecl>();
	    exns = new ArrayList<String>();
	    cond_exns = new ArrayList<String>();
	    current_path = new Stack<JCExpression>();
	    current_signs = new Stack<Integer>();
	    types = ctx.getProcEnv().getTypeUtils();
	    throwstack = new Stack<HashSet<Object[]>>();
	    appstack = new Stack<HashSet<Object[]>>();
	    throwmap = new HashMap<JCMethodDecl,HashSet<Object[]>>();
	    callmap = new HashMap<JCMethodDecl,HashSet<Object[]>>();
	    methdefs = new HashMap<Symbol,JCMethodDecl>();
	    locmap = new HashMap<JCMethodDecl,String>();
	    untraceable_branches = 0;
	    // XXX TODO: untraceables left to do: loops(4),break or return? goto?
	    writer = new PrintWriter(new FileWriter(splatfile));
	}
	private String className = "";
	public void visitTopLevel(JCCompilationUnit tree) {
	    ctx.info("XXXXXXXXXX NEW TOP LEVEL");
	    super.visitTopLevel(tree);
	}
	public void visitClassDef (JCClassDecl tree) {

	    //ctx.info("Visiting class "+ (tree.name.length()==0 ? "<anon class>" : tree.name));
	    //ctx.info("Symbol: "+tree.sym);
	    String oldName = className;
	    if (className.equals("")) {
		this.className = tree.getSimpleName().toString();
	    } else {
		this.className += "$" + tree.getSimpleName().toString();
	    }
	    current_class.push(tree);
	    super.visitClassDef(tree);
	    current_class.pop();
	    className = oldName;
	}
	public String splatException(TypeMirror type,
				     Stack<JCExpression> throwPath,
				     Stack<Integer> throwSigns,
				     boolean sometimes) {
	    // TODO: Need to actually expand the whole stack, not just top element
	    if (sometimes || throwPath.empty()) {
		return type+" under some nontrivial conditions";
	    } else {
	        return type+" when "+(throwSigns.peek() == 0 ? "!(" : "(")+throwPath.peek()+")";
	    }
	}
	private String resolveParameterList(JCMethodDecl tree) {
		String paramList = "";
		boolean first = true;
		
		for (JCVariableDecl var : tree.getParameters()) {
			String str = resolveTypeToString(var.getType());
			
			
			if (first) {
				first = false;
				paramList = str;
			} 
			else {
				paramList = paramList + "," + str;					
			}
		}
		
		
		return paramList;
	}
	// Helper method to resolve a JCTree which is supposed
	// to represent a type into a string which can be
	// used to generate a key for the hash map.
	private String resolveTypeToString(JCTree type) {
		String typeString = "";
		if (type == null) {
			return "void";
		}
		
		if (type instanceof JCTree.JCTypeApply) {
			type = ((JCTree.JCTypeApply) type).getType();
		}
		
		if (type instanceof JCTree.JCIdent) {
			typeString = ((JCTree.JCIdent) type).name.toString();//sym.getQualifiedName().toString();
		}
		else if (type instanceof JCTree.JCPrimitiveTypeTree) {
			typeString = ((JCTree.JCPrimitiveTypeTree) type).getPrimitiveTypeKind().toString().toLowerCase();
		}
		else if (type instanceof JCTree.JCArrayTypeTree) {
			typeString = resolveTypeToString(((JCTree.JCArrayTypeTree) type).elemtype) + "[]";
		}
		else {
			typeString = type.getClass().getName();					
		}
		
		return typeString;
	}
	public void visitMethodDef (JCMethodDecl tree) {
	    if (tree == null) {
		throw new IllegalArgumentException("Method declaration shouldn't be null!");
	    }
	    //ctx.info("Visiting method "+tree.name+" in "+tree.sym+" of "+tree.sym.owner);
	    if (tree.sym != null) {
		exns.clear();
		cond_exns.clear();
	    }
	    methdefs.put(tree.sym,tree);
	    current_method.push(tree);
	    throwstack.push(new HashSet<Object[]>());
	    appstack.push(new HashSet<Object[]>());
	    untraceable_branches = 0;
	    super.visitMethodDef(tree);
	    if (tree.sym != null) {
		String exnlist = "";
		String condexns = "";
		String loc = compunit.getPackageName()+"."+this.className+"("+resolveTypeToString(tree.getReturnType())+")"+tree.sym;
		locmap.put(tree,loc);
		String splat = "";
		//String loc = compunit.sourcefile+":"+tree.getStartPosition();
		// Trust me, if you're crying reading this, imagine how I feel writing it
		// Stupid compiler bugs.
		HashSet<TypeMirror> alreadyThrown = new HashSet<TypeMirror>();
		for (Object[] ex : throwstack.peek()) {
		    TypeMirror throwType = (TypeMirror)ex[0];
		    Stack<JCExpression> throwPath = (Stack<JCExpression>)ex[1];
		    Stack<Integer> throwSigns = (Stack<Integer>)ex[2];
		    boolean sometimes = (boolean)(Boolean)ex[3];
		    if (!alreadyThrown.contains(throwType) || !sometimes) {
			splat += "<br>"+splatException(throwType, throwPath, throwSigns, sometimes);
			alreadyThrown.add(throwType);
		    }
		}
		if (!throwstack.peek().isEmpty()) {
		    ctx.info("splatting: "+loc+"=>"+splat);
		    writer.println(loc+"-JAVAGROK-"+splat);
		}

	    }
	    current_method.pop();
	    throwmap.put(tree,throwstack.pop());
	    callmap.put(tree,appstack.pop());
	}
	public void visitIf(JCIf tree) {
	    scan(tree.cond);
	    current_path.push(tree.cond);
	    current_signs.push(1);
	    scan(tree.thenpart);
	    current_signs.pop();
	    current_signs.push(0);
	    scan(tree.elsepart);
	    current_path.pop();
	    current_signs.pop();
	}
	public void visitConditional(JCConditional tree) {
	    scan(tree.cond);
	    current_path.push(tree.cond);
	    current_signs.push(1);
	    scan(tree.truepart);
	    current_signs.pop();
	    current_signs.push(0);
	    scan(tree.falsepart);
	    current_path.pop();
	    current_signs.pop();
	}
	public void visitSwitch(JCSwitch tree) {
	    scan(tree.selector);
	    untraceable_branches++;
	    for (JCCase c : tree.cases) {
		scan(c);
	    }
	    untraceable_branches--;
	}
	private String extractExceptionType(JCExpression e) {
	    if (e.getKind() == Tree.Kind.NEW_CLASS)
		return ((JCNewClass)e).getIdentifier().toString();
	    else
		return "Unknown: "+e.getKind()+"("+e+")";
	}
	private String getCurrentBranchPath() {
	    // TODO: Need to actually expand the whole stack, not just top element
	    if (current_path.empty()) {
		// TODO: This isn't always (pun not intended) right either; there are some methods that always
		// return under conditionals, and the last statement is a throw.  They're not *always* going to
		// throw the exception.  Maybe we should just say 'under some circumstances'
		return " sometimes";
	    }
	    else {
	        return " when "+(current_signs.peek() == 0 ? "!(" : "(")+current_path.peek()+")";
	    }
	}
	private TypeMirror typeToMirror(Type t) {
	    return t.asElement().asType();
	}
	public void visitTry(JCTry tree) {
	    throwstack.push(new HashSet<Object[]>());
	    appstack.push(new HashSet<Object[]>());
	    scan(tree.body);
	    untraceable_branches++;
	    for (JCCatch c : tree.catchers) {
		scan(c);
	    }
	    untraceable_branches--;
	    scan(tree.finalizer);
	    HashSet<Object[]> exns = throwstack.pop();
	    throwstack.peek().addAll(exns);
	    HashSet<Object[]> calls = appstack.pop();
	    appstack.peek().addAll(calls);
	}
	public void visitCatch(JCCatch tree) {
	    // XXX TODO: If a subsequent catch for the same try block catches
	    // a superclass of an exception thrown in this catch's body,
	    // right now that catch will remove from the list.  We need
	    // another stack for exceptions thrown from the current set of catches...
	    TypeMirror caught = typeToMirror(tree.param.type);
	    HashSet<Object[]> toremove = new HashSet<Object[]>();
	    HashSet<TypeMirror> deferredfilter = new HashSet<TypeMirror>();
	    for (Object[] ex : throwstack.peek()) {
		if (types.isSubtype((TypeMirror)ex[0],caught)) {
		    toremove.add(ex);
		    //ctx.info("XXXX filtering exception "+(TypeMirror)ex[0]);
		}
		deferredfilter.add(caught);
	    }
	    throwstack.peek().removeAll(toremove);
	    for (Object[] app : appstack.peek()) {
		((HashSet<TypeMirror>)app[4]).add(caught);
	    }

	    // test code for above:
	    //try {
	    //    throw new RuntimeException();
	    //}
	    //catch (RuntimeException e) {
	    //}
	}
	public void visitThrow(JCThrow tree) {
	    super.visitThrow(tree);
	    String exn = extractExceptionType(tree.getExpression());
	    Type t = tree.getExpression().type;
	    TypeMirror ty = t.asElement().asType();
	    //ctx.info("***"+current_class.peek().name+"."+current_method.peek().name+" throws "+ty+getCurrentBranchPath());
	    if (!exns.contains(exn))
		exns.add(exn);
	    cond_exns.add(exn+getCurrentBranchPath());
	    // This is totally awful, but we can't new any custom objects here for whatever reason...
	    Object[] ex = new Object[4];
	    ex[0] = ty;
	    ex[1] = (Stack<JCExpression>)current_path.clone();
	    ex[2] = (Stack<Integer>)current_signs.clone();
	    if (untraceable_branches > 0)
	        ex[3] = true;
	    else
		ex[3] = false;
	    throwstack.peek().add(ex);
	}
	public void visitApply(JCMethodInvocation tree) {
	    super.visitApply(tree);
	    //ctx.info("**Importing: "+tree.meth.sym+" owned by "+tree.meth.sym.owner+" |"+tree.meth.type);
	    // I think this is what happens for static / class initializers - we're not in a method
	    if (!appstack.isEmpty()) {
		Object[] application = new Object[5];
		application[0] = tree;
		application[1] = (Stack<JCExpression>)current_path.clone();
		application[2] = (Stack<Integer>)current_signs.clone();
		if (untraceable_branches > 0)
		    application[3] = true;
		else
		    application[3] = false;
		application[4] = new HashSet<TypeMirror>();
		appstack.peek().add(application);
	    }
	    if (tree.meth.getKind() == Tree.Kind.MEMBER_SELECT) {
	        JCFieldAccess access = (JCFieldAccess)tree.meth;
		//ctx.info("Need to import exception analysis for "+access.type+" "+access.selected.type+"."+access.name);
		//ctx.info("Symbol: "+access.sym+" owned by "+access.sym.owner);
		if (access.type != null && access.type.getThrownTypes() != null && !access.type.getThrownTypes().isEmpty()) {
		    // XXX TODO
		    //ctx.info("*** This method ("+access.selected.type+"."+access.sym.getQualifiedName()+")might throw!");
		}
		if (methdefs.get(access.sym) != null) {
		    ctx.info("Found existing definition for "+access.sym);
		    ctx.info("throwmap has "+throwmap.get(methdefs.get(access.sym)).size()+" throws for it.");
		}
	        //if (access.selected.getKind() == Tree.Kind.IDENTIFIER) {
	        //    JCIdent id = (JCIdent)access.selected;
	        //    ctx.info("MORE INFO: "+id.type+"/"+id.sym);
	        //}
	    } else if (tree.meth.getKind() == Tree.Kind.IDENTIFIER) {
	        JCIdent id = (JCIdent)tree.meth;
		//ctx.info("Need to import exception analysis for "+id.sym+" with type "+id.type);
	    } else {
	        ctx.info("Method invocation on SOMETHING");
	    }
	}

	private JCCompilationUnit compunit;
	public void setCompilationUnit(CompilationUnitTree unit) {
	    compunit = (JCCompilationUnit)unit;
	}
	private Symbol getSymbol(JCExpression exp) {
	    if (exp.getKind() == Tree.Kind.MEMBER_SELECT) {
		return ((JCFieldAccess)exp).sym;
	    }
	    else if (exp.getKind() == Tree.Kind.IDENTIFIER) {
		return ((JCIdent)exp).sym;
	    }
	    else {
		return null;
	    }
	}
	private void propagateFromToWithArgs(JCMethodDecl callee, JCMethodDecl caller, List<JCExpression> args) {
	    // args is a list of actual arguments, the formals are in callee.params
	    // We want to propagate exceptions from callee to caller if the args contain formals of the caller.
	    // We use the args to replace the callee's formals in the conditions for the exceptions it throws.
	}
	public void finished() {
	    writer.close();
	    ctx.info("throwmap has "+throwmap.size()+" entries.");
	    for (JCMethodDecl m : throwmap.keySet()) {
		HashSet<Object[]> exns = throwmap.get(m);
		for (Object[] ex : exns) {
		    ctx.info("Method "+m.name+" throws "+(TypeMirror)ex[0]);
		}
	    }
	    ctx.info("callmap has "+throwmap.size()+" entries.");
	    ctx.info("methdefs has "+methdefs.size()+" entries.");

	    // Now for the big time: propagation:
	    for (JCMethodDecl m : callmap.keySet()) {
		for (Object[] app : callmap.get(m)) {
		    JCMethodInvocation inv = (JCMethodInvocation)app[0];
		    //ctx.info("Looking up invocation of "+getSymbol(inv.meth)+" in "+m.sym);
		    JCMethodDecl target = methdefs.get(getSymbol(inv.meth));
		    //ctx.info("Found target: "+target);
		    // target is null if the method call is to a core class or such, we didn't analyze it
		    if (target != null && !throwmap.get(target).isEmpty()) {
			ctx.info("Need to propagate exceptions from "+target.getName()+" to "+m.getName());
			propagateFromToWithArgs(target, m, inv.args);
		    }
		}
	    }
	}
    }
    @Override // from AbstractProcessor
    public void init (final ProcessingEnvironment procEnv)
    {
        super.init(procEnv);

        // we need to reach into javac's guts, so make sure we're running therein
        if (!(procEnv instanceof JavacProcessingEnvironment)) {
            procEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING, "JavaGrok requires javac v1.6+.");
            return;
        }

        // some of the guts we need to reach into are protected, so Backdoor handles all of our
        // reflection-based access control circumvention; yee haw!
        Backdoor.init(procEnv);

        // the analysis context liases between javac internals and the analyzers
        _ctx = new AnalysisContextImpl(procEnv);

        _ctx.info("JavaGrok:ExceptionProcessor running");
	try {
	    String splatfile = ".splat";
	    File f = new File(splatfile);
	    if (f.exists()) {
		f.delete();
	    }
	    f.createNewFile();
	    scanner = new ExceptionScanner(_ctx, splatfile);
	}
	catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException("Cannot perform exception analysis with file error!");
	}
    }

    public void typeProcess(TypeElement element, TreePath tree)
    {
	_ctx.info("Type-processing "+element);
	//elements.addAll(element.getEnclosedElements());
	if (scanner != null) {
	    scanner.setCompilationUnit(tree.getCompilationUnit());
	    ((JCTree)tree.getLeaf()).accept(scanner);
	}
    }

    public void typeProcessingOver()
    {
        // run our analyzers in turn
        //for (Analyzer a : _analyzers) {
        //    try {
        //        a.process(_ctx, elements);
        //    } catch (Exception e) {
        //        _ctx.warn("Analyzer failed in process() '" + a + "': " + e);
        //        e.printStackTrace(System.err);
        //    }
        //}
	if (scanner != null) {
	    scanner.finished();
	}
    }

    protected ExceptionScanner scanner;
    protected AnalysisContextImpl _ctx;
    protected List<Analyzer> _analyzers = new ArrayList<Analyzer>();
    protected Set<Element> elements = new HashSet<Element>();

    protected static final String SERVICE_NAME = "org.javagrok.analysis.ExceptionAnalyzer";
}
