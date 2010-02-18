//
// $Id$

package org.javagrok.analysis;

import java.util.Set;
import javax.lang.model.element.Element;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.comp.Attr;
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
	    current_path = new Stack<Pair<BoolSign,JCExpression>>();
	}
	public void visitClassDef (JCClassDecl tree) {
	    //ctx.info("Visiting class "+ (tree.name.length()==0 ? "<anon class>" : tree.name));
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
	    //ctx.info("Visiting method "+tree.name+" in "+tree.sym);
	    if (tree.sym != null) {
		exns.clear();
	    }
	    current_method.push(tree);
	    super.visitMethodDef(tree);
	    if (tree.sym != null && !exns.isEmpty()) {
		String exnlist = "";
		for (String s : exns) {
		    exnlist += " " + s;
		}
		ctx.info("Method "+current_class.peek().name+"."+tree.name+" throws"+exnlist);
		ctx.addAnnotation(tree, ExceptionProperty.class,
				  "exceptionsThrown", "explicitly throws"+exnlist);
	    }
	    current_method.pop();
	    ///* XXX WIP */
	    //List<JCExpression> thrown = tree.getThrows();
	    //for (JCExpression e : thrown) {
	    //    JCIdent id = (JCIdent)e;
	    //    ctx.info("Method "+tree.name+" throws "+id.getName().toString());
	    //}
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
		return " always";
	    }
	    else {
	        return " when "+(current_path.peek().a == BoolSign.NEG ? "!(" : "(")+current_path.peek().b+")";
	    }
	}
	public void visitThrow(JCThrow tree) {
	    super.visitThrow(tree);
	    String exn = extractExceptionType(tree.getExpression());
	    ctx.info(current_class.peek().name+"."+current_method.peek().name+" throws "+exn+getCurrentBranchPath());
	    if (!exns.contains(exn))
		exns.add(exn);
	}
	public void visitApply(JCMethodInvocation tree) {
	    super.visitApply(tree);
	    ctx.info("Need to import exception analysis for "+tree.meth + " with type "+tree.meth.type);
	    if (tree.meth.getKind() == Tree.Kind.MEMBER_SELECT) {
	        JCFieldAccess access = (JCFieldAccess)tree.meth;
	        ctx.info("Member select on "+access.name+" of expression "+access.selected+"("+access.selected.getKind()+")");
	        if (access.selected.getKind() == Tree.Kind.IDENTIFIER) {
	            JCIdent id = (JCIdent)access.selected;
	            ctx.info("MORE INFO: "+id.type+"/"+id.sym);
	        }
	    //} else if (tree.meth.getKind() == Tree.Kind.IDENTIFIER) {
	    //    JCIdent id = (JCIdent)tree.meth;
	    //    ctx.info("Identifier "+id+" of type "+id.type);
	    //} else {
	    //    ctx.info("Method invocation on SOMETHING");
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
        for (Element elem : elements) {
            // we'll get an Element for each top-level class in a compilation unit (source file),
            // but scanning the AST from the top-level compilation unit will visit all classes
            // defined therein, which would result in adding annotations multiple times for source
            // files that define multiple top-level classes; so we specifically find the
            // JCClassDecl in the JCCompilationUnit that corresponds to the class we're processing
            // and only traverse its AST subtree
            Symbol.ClassSymbol csym = (Symbol.ClassSymbol)elem;
            JCCompilationUnit unit = ctx.getCompilationUnit(elem);
	    Attr attr = Attr.instance(ctx.getInnerContext());
            for (JCTree def : unit.defs) {
                if (def.getTag() == JCTree.CLASSDEF && ((JCClassDecl)def).name == csym.name) {
		    //def.accept(attr);
		    //attr.visitClassDef((JCClassDecl)def);
		    attr.attribClass(def.pos(), csym);
                    def.accept(new ExceptionScanner(ctx));
                }
            }
        }
    }
}
