//
// $Id$

package org.javagrok.processor;

import java.lang.annotation.Annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import org.javagrok.analysis.AnalysisContext;

/**
 * Provides an implementation of the {@link AnalysisContext}.
 */
public class AnalysisContextImpl implements AnalysisContext
{
    public AnalysisContextImpl (ProcessingEnvironment procEnv)
    {
        _procEnv = procEnv;
        _trees = Trees.instance(procEnv);

        final Context ctx = ((JavacProcessingEnvironment)procEnv).getContext();
        _tmaker = TreeMaker.instance(ctx);
        _names = Names.instance(ctx);
        _annotate = Annotate.instance(ctx);
        _syms = Symtab.instance(ctx);
        _enter = Enter.instance(ctx);
	_ctx = ctx;
    }

    // from interface AnalysisContext
    public JCCompilationUnit getCompilationUnit (Element elem)
    {
        TreePath path = _trees.getPath(elem);
        return (path == null) ? null : (JCCompilationUnit)path.getCompilationUnit();
    }

    // from interface AnalysisContext
    public void addAnnotation (JCClassDecl cdecl, Class<? extends Annotation> aclass,
                               String... argsVals)
    {
        addAnnotation(cdecl.mods, cdecl.sym, aclass, argsVals);
        // System.out.println("Annotated " + cdecl);
    }

    // from interface AnalysisContext
    public void addAnnotation (JCMethodDecl mdecl, Class<? extends Annotation> aclass,
                               String... argsVals)
    {
        addAnnotation(mdecl.mods, mdecl.sym, aclass, argsVals);
        // System.out.println("Annotated " + mdecl);
    }

    // from interface AnalysisContext
    public void addAnnotation (JCVariableDecl pdecl, Class<? extends Annotation> aclass,
                               String... argsVals)
    {
        addAnnotation(pdecl.mods, pdecl.sym, aclass, argsVals);
        // System.out.println("Annotated " + pdecl);
    }

    // from interface AnalysisContext
    public void info (String message)
    {
        _procEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    // from interface AnalysisContext
    public void warn (String message)
    {
        _procEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    protected void addAnnotation (JCModifiers mods, Symbol sym,
                                  Class<? extends Annotation> aclass, String... argsVals)
    {
        if (argsVals.length % 2 != 0) {
            throw new IllegalArgumentException("Mismatched argsVals array");
        }

        // TODO: make sure the annotation isn't already there
        List<JCExpression> args = List.nil();
        for (int ii = 0; ii < argsVals.length; ii += 2) {
            args = args.prepend(_tmaker.Assign(_tmaker.Ident(_names.fromString(argsVals[ii])),
                                               _tmaker.Literal(TypeTags.CLASS, argsVals[ii+1])));
        }

        JCAnnotation a = _tmaker.Annotation(mkFA(aclass.getName()), args);
        mods.annotations = mods.annotations.prepend(a);

        // if the annotations AST has already been resolved into type symbols, we have to manually
        // add a type symbol for annotation to the Class, Method or VarSymbol; if it has not been
        // resolved, then we're OK because javac will later resolve the AST node added above
        if (sym != null) {
            sym.attributes_field = sym.attributes_field.prepend(
                Backdoor.enterAnnotation(_annotate, a, _syms.annotationType,
                                         _enter.getEnv(getOwningType(sym))));
        }
    }

    protected Symbol.TypeSymbol getOwningType (Symbol sym)
    {
        return (sym instanceof Symbol.TypeSymbol) ? (Symbol.TypeSymbol)sym :
            getOwningType(sym.owner);
    }

    protected JCExpression mkFA (String fqName) {
        int didx = fqName.lastIndexOf(".");
        if (didx == -1) {
            return _tmaker.Ident(_names.fromString(fqName)); // simple identifier
        } else {
            return _tmaker.Select(mkFA(fqName.substring(0, didx)), // nested FA expr
                                  _names.fromString(fqName.substring(didx+1)));
        }
    }

    public Context getInnerContext() {
	return _ctx;
    }

    protected ProcessingEnvironment _procEnv;
    protected Trees _trees;
    protected TreeMaker _tmaker;
    protected Names _names;
    protected Annotate _annotate;
    protected Symtab _syms;
    protected Enter _enter;
    protected final Context _ctx;
}
