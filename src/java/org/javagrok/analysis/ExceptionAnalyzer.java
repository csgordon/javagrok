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
import com.sun.tools.javac.main.JavaCompiler;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

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
	private HashMap<String,String> map;
	private JCCompilationUnit compunit;
	private String className = "";

	public ExceptionScanner(final AnalysisContext ctx) {
	    this.ctx = ctx;
	    try {
		File f = new File(".splat");
		if (f.exists()) {
		    // build map
		    map = new HashMap<String,String>();
		    BufferedReader reader = new BufferedReader(new FileReader(".splat"));
		    while (reader.ready()) {
			String line = reader.readLine();
			ctx.info("::"+line);
			// Should really have a better delimiter...
			String[] parts = line.split("-JAVAGROK-");
			map.put(parts[0],parts[1]);
			ctx.info("Mapping "+parts[0]+" to "+parts[1]);
		    }
		    reader.close();
		} else {
		    map = null;
		}
	    }
	    catch (IOException e) {
		throw new RuntimeException("IO Error!");
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
	public void setCompilationUnit(JCCompilationUnit unit) {
	    //ctx.info("ExceptionAnalyzer examining "+unit.sourcefile);
	    compunit = unit;
	}
	public void visitClassDef(JCClassDecl tree) {
	    String oldName = className;
	    if (className.equals("")) {
		this.className = tree.getSimpleName().toString();
	    } else {
		this.className += "$" + tree.getSimpleName().toString();
	    }
	    super.visitClassDef(tree);
	    className = oldName;
	}

	public void visitMethodDef (JCMethodDecl tree) {
	//    ctx.info("Visiting method "+tree.name+" in "+tree.sym);
	//    ctx.info("Visiting method "+compunit.getPackageName()+"."+this.className
	//	    +"("+resolveTypeToString(tree.getReturnType())+")"+tree.sym//tree.getName()+"("+resolveParameterList(tree)+")"
	//	    );
	    if (map != null) {
		//String location = compunit.sourcefile+":"+tree.getStartPosition();
		String location = compunit.getPackageName()+"."+this.className+"("+resolveTypeToString(tree.getReturnType())+")"+tree.sym;
		String annot = map.get(location);
		if (annot != null) {
		    ctx.info("Method "+tree.name+" annotated: "+annot);
		    ctx.addAnnotation(tree, ExceptionProperty.class,
				      "throwsWhen", annot);
		}
		else {
		    //ctx.info("Didn't match "+location);
		}
	    }
	    super.visitMethodDef(tree);
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
	    ExceptionScanner scanner = new ExceptionScanner(ctx);
	    for (Element elem : elements) {
		// we'll get an Element for each top-level class in a compilation unit (source file),
		// but scanning the AST from the top-level compilation unit will visit all classes
		// defined therein, which would result in adding annotations multiple times for source
		// files that define multiple top-level classes; so we specifically find the
		// JCClassDecl in the JCCompilationUnit that corresponds to the class we're processing
		// and only traverse its AST subtree
		Symbol.ClassSymbol csym = (Symbol.ClassSymbol)elem;
		JCCompilationUnit unit = ctx.getCompilationUnit(elem);
		scanner.setCompilationUnit(unit);
		for (JCTree def : unit.defs) {
		    if (def.getTag() == JCTree.CLASSDEF && ((JCClassDecl)def).name == csym.name) {
			def.accept(scanner);
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
