//
// $Id$

package org.javagrok.analysis;

import java.util.Set;
import javax.lang.model.element.Element;

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

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Exception analysis
 */
public class ExceptionAnalyzer extends AbstractAnalyzer
{
    private static enum BoolSign {
	POS, NEG
    }
    private class ExceptionScanner extends TreeScanner {
	private final AnalysisContext ctx;
	private List<String> exns;
	private List<String> cond_exns;
	private Stack<Pair<BoolSign,JCExpression>> current_path;
	private Stack<JCClassDecl> current_class;
	private Stack<JCMethodDecl> current_method;
	private class Pair<X,Y> {
	    public X a;
	    public Y b;
	    public Pair(X a, Y b) {
		this.a = a;
		this.b = b;
	    }
	}
	public ExceptionScanner(final AnalysisContext ctx) {
	    this.ctx = ctx;
	    current_class = new Stack<JCClassDecl>();
	    current_method = new Stack<JCMethodDecl>();
	    exns = new ArrayList<String>();
	    cond_exns = new ArrayList<String>();
	    current_path = new Stack<Pair<BoolSign,JCExpression>>();
	}
	public void visitClassDef (JCClassDecl tree) {
	    ctx.info("Visiting class "+ (tree.name.length()==0 ? "<anon class>" : tree.name));
	    ctx.info("Symbol: "+tree.sym);
	    //if (tree.name.length() > 0) {
	    //    ctx.addAnnotation(tree, ExceptionProperty.class,
	    //    		  "property", tree.name + " analyzed by exception analysis!");
	    //}
	    current_class.push(tree);
	    super.visitClassDef(tree);
	    current_class.pop();
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
	    current_method.push(tree);
	    super.visitMethodDef(tree);
	    if (tree.sym != null) {
		String exnlist = "";
		String condexns = "";
		if (!exns.isEmpty()) {
		    for (String s : exns) {
			exnlist += " " + s;
		    }
		}
		if (!cond_exns.isEmpty()) {
		    for (String s : cond_exns) {
			condexns += "<br>" + s;
		    }
		}
		if (exnlist.length() > 0 || condexns.length() > 0) {
		    //ctx.info("Method "+current_class.peek().name+"."+tree.name+" throws"+exnlist);
		    ctx.addAnnotation(tree, ExceptionProperty.class,
				      "exceptionsThrown", "explicitly throws"+exnlist,
				      "throwsWhen", condexns);
		}
	    }
	    current_method.pop();
	}
	public void visitIf(JCIf tree) {
	    scan(tree.cond);
	    current_path.push(new Pair<BoolSign,JCExpression>(BoolSign.POS,tree.cond));
	    scan(tree.thenpart);
	    current_path.peek().a = BoolSign.NEG;
	    scan(tree.elsepart);
	    current_path.pop();
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
		return " always";
	    }
	    else {
	        return " when "+(current_path.peek().a == BoolSign.NEG ? "!(" : "(")+current_path.peek().b+")";
	    }
	}
	public void visitThrow(JCThrow tree) {
	    super.visitThrow(tree);
	    String exn = extractExceptionType(tree.getExpression());
	    //ctx.info(current_class.peek().name+"."+current_method.peek().name+" throws "+exn+getCurrentBranchPath());
	    if (!exns.contains(exn))
		exns.add(exn);
	    cond_exns.add(exn+getCurrentBranchPath());
	}
	public void visitApply(JCMethodInvocation tree) {
	    super.visitApply(tree);
	    //ctx.info("**Importing: "+tree.meth.sym+" owned by "+tree.meth.sym.owner+" |"+tree.meth.type);
	    if (tree.meth.getKind() == Tree.Kind.MEMBER_SELECT) {
	        JCFieldAccess access = (JCFieldAccess)tree.meth;
		//ctx.info("Need to import exception analysis for "+access.type+" "+access.selected.type+"."+access.name);
		//ctx.info("Symbol: "+access.sym+" owned by "+access.sym.owner);
		if (access.type != null && access.type.getThrownTypes() != null && !access.type.getThrownTypes().isEmpty()) {
		    //ctx.info("*** This method might throw!");
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
    }

    // from interface Analyzer
    public void init (AnalysisContext ctx)
    {
        ctx.info("Exception analysis initialized");
    }

    // from interface Analyzer
    public void process (final AnalysisContext ctx, Set<? extends Element> elements)
    {
	try {
        for (Element elem : elements) {
            // we'll get an Element for each top-level class in a compilation unit (source file),
            // but scanning the AST from the top-level compilation unit will visit all classes
            // defined therein, which would result in adding annotations multiple times for source
            // files that define multiple top-level classes; so we specifically find the
            // JCClassDecl in the JCCompilationUnit that corresponds to the class we're processing
            // and only traverse its AST subtree
            Symbol.ClassSymbol csym = (Symbol.ClassSymbol)elem;
            JCCompilationUnit unit = ctx.getCompilationUnit(elem);
	    Enter e = Enter.instance(ctx.getInnerContext());
	    //e.visitTopLevel(unit);
	    Todo todo = Todo.instance(ctx.getInnerContext());
	    Attr attr = Attr.instance(ctx.getInnerContext());
	    for (Env<AttrContext> env : todo) {
	        attr.attribClass(env.tree.pos(), env.enclClass.sym);
	        //compileStates.put(env, CompileState.ATTR);
	    }
            for (JCTree def : unit.defs) {
                if (def.getTag() == JCTree.CLASSDEF && ((JCClassDecl)def).name == csym.name) {
		    //def.accept(attr);
		    //attr.visitClassDef((JCClassDecl)def);
		    //e.visitClassDef((JCClassDecl)def);
		    //attr.attribClass(def.pos(), csym);
                    def.accept(new ExceptionScanner(ctx));
                }
            }
        }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
