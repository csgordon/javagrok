//
// $Id$

package org.javagrok.analysis;

import java.util.Set;
import javax.lang.model.element.Element;

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
    public void process (AnalysisContext ctx, Set<? extends Element> elements)
    {
        for (Element elem : elements) {
            ctx.info("NOOP! " + elem);
        }
    }
}
