//
// $Id$

package org.javagrok.analysis;

import java.util.Set;
import javax.lang.model.element.Element;

/**
 * The main entry point for an analysis.
 */
public interface Analyzer
{
    /**
     * Called to initialize all analyses prior to calling {@link #process}.
     */
    public void init (AnalysisContext ctx);

    /**
     * Called to perform analysis.
     */
    public void process (AnalysisContext ctx, Set<? extends Element> elements);
}
