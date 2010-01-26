//
// $Id$

package org.javagrok.analysis;

import java.util.Set;
import javax.lang.model.element.Element;

import com.sun.tools.javac.tree.JCTree.*;
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
            JCCompilationUnit unit = ctx.getCompilationUnit(elem);
            ctx.info("NOOP! " + elem + " " + unit.sourcefile);

            unit.accept(new TreeScanner() {
                public void visitClassDef (JCClassDecl tree) {
                    ctx.addAnnotation(tree, Property.class, "property", "NOOP analyzed!");
                }
            });
        }
    }
}
