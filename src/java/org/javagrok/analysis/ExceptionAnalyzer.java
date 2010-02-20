//
// $Id$

package org.javagrok.analysis;

import java.util.Set;
import javax.lang.model.element.Element;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.source.tree.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Exception analysis
 */
public class ExceptionAnalyzer extends AbstractAnalyzer
{
    private class ExceptionScanner extends TreeScanner {
	private final AnalysisContext ctx;
	private List<String> exns;
	private JCClassDecl current_class;
	public ExceptionScanner(final AnalysisContext ctx) {
	    this.ctx = ctx;
	}
	public void visitClassDef (JCClassDecl tree) {
	    //ctx.info("Visiting class "+ (tree.name.length()==0 ? "<anon class>" : tree.name));
	    if (tree.name.length() > 0) {
		ctx.addAnnotation(tree, Property.class,
				  "property", tree.name + " analyzed by exception analysis!");
		current_class = tree;
	    }
	    super.visitClassDef(tree);
	    if (tree.name.length() > 0) {
		current_class = null;
	    }
	}
	public void visitMethodDef (JCMethodDecl tree) {
	    if (tree == null) {
		ctx.info("NULL MethodDecl!!!");
	    }
	    //ctx.info("Visiting method "+tree.name+" in "+tree.sym);
	    if (tree.sym != null) {
		exns = new ArrayList<String>();
	    }
	    super.visitMethodDef(tree);
	    if (tree.sym != null && !exns.isEmpty()) {
		String exnlist = "";
		for (String s : exns) {
		    exnlist += " " + s;
		}
		ctx.info("Method "+current_class.name+"."+tree.name+" throws"+exnlist);
		ctx.addAnnotation(tree, Property.class,
				  "property", "explicitly throws"+exnlist);
		//for (JCVariableDecl param : tree.params) {
		//    ctx.addAnnotation(param, Property.class,
		//		      "property", param.name + " analyzed by exception analysis!");
		//}
	    }
	    ///* XXX WIP */
	    //List<JCExpression> thrown = tree.getThrows();
	    //for (JCExpression e : thrown) {
	    //    JCIdent id = (JCIdent)e;
	    //    ctx.info("Method "+tree.name+" throws "+id.getName().toString());
	    //}
	}
	private String extractExceptionType(JCExpression e) {
	    if (e.getKind() == Tree.Kind.NEW_CLASS)
		return ((JCNewClass)e).getIdentifier().toString();
	    else
		return "Unknown: "+e.getKind()+"("+e+")";
	}
	public void visitThrow(JCThrow tree) {
	    super.visitThrow(tree);
	    String exn = extractExceptionType(tree.getExpression());
	    ctx.info("Found throw of type " + exn);
	    if (!exns.contains(exn))
		exns.add(exn);
	}
	public void visitApply(JCMethodInvocation tree) {
	    super.visitApply(tree);
	    //ctx.info("Need to import exception analysis for "+tree.meth + " with type "+tree.meth.getKind());
	    //if (tree.meth.getKind() == Tree.Kind.MEMBER_SELECT) {
	    //    JCFieldAccess access = (JCFieldAccess)tree.meth;
	    //    ctx.info("Member select on "+access.name+" of expression "+access.selected+"("+access.selected.getKind()+")");
	    //    if (access.selected.getKind() == Tree.Kind.IDENTIFIER) {
	    //        JCIdent id = (JCIdent)access.selected;
	    //        ctx.info("MORE INFO: "+id.type+"/"+id.sym);
	    //    }
	    //} else if (tree.meth.getKind() == Tree.Kind.IDENTIFIER) {
	    //    JCIdent id = (JCIdent)tree.meth;
	    //    ctx.info("Identifier "+id+" of type "+id.type);
	    //} else {
	    //    ctx.info("Method invocation on SOMETHING");
	    //}
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
            for (JCTree def : unit.defs) {
                if (def.getTag() == JCTree.CLASSDEF && ((JCClassDecl)def).name == csym.name) {
                    def.accept(new ExceptionScanner(ctx));
                }
            }
        }
    }
}
