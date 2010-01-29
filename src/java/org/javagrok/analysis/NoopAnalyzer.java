//
// $Id$

package org.javagrok.analysis;

import java.util.Set;
import javax.lang.model.element.Element;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

/**
 * A simple NOOP analysis for testing.
 */
public class NoopAnalyzer extends AbstractAnalyzer
{
    // from interface Analyzer
    public void init (AnalysisContext ctx)
    {
        ctx.info("NOOP analysis initialized");
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
                    def.accept(new TreeScanner() {
                        public void visitClassDef (JCClassDecl tree) {
                            ctx.addAnnotation(tree, Property.class,
                                              "property", tree.name + " analyzed!");
                            super.visitClassDef(tree);
                        }
                        public void visitMethodDef (JCMethodDecl tree) {
                            ctx.addAnnotation(tree, Property.class,
                                              "property", tree.name + " analyzed!");
                            for (JCVariableDecl param : tree.params) {
                                ctx.addAnnotation(param, Property.class,
                                                  "property", param.name + " analyzed!");
                            }
                        }
                    });
                }
            }
        }
    }
}
