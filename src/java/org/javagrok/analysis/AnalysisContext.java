//
// $Id$

package org.javagrok.analysis;

/**
 * Provides useful services to an analysis.
 */
public interface AnalysisContext
{
    // TODO: mechanisms for reading/emitting annotations

    /** Emits an informational log message. */
    public void info (String message);

    /** Emits a warning log message. */
    public void warn (String message);
}
