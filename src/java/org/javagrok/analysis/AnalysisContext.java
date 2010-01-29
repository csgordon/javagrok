//
// $Id$

package org.javagrok.analysis;

import java.lang.annotation.Annotation;
import javax.lang.model.element.Element;

import com.sun.tools.javac.tree.JCTree;

/**
 * Provides useful services to an analysis.
 */
public interface AnalysisContext
{
    /**
     * Returns the internal javac AST root for the supplied element.
     */
    public JCTree.JCCompilationUnit getCompilationUnit (Element elem);

    /**
     * Adds an annotation to the supplied class AST node.
     *
     * @param cdecl the class onto which to add the annotation.
     * @param aclass the class of the annotation to be added.
     * @param argsVals an array of strings representing the annotation field names and values as
     * name, value, name value, etc. (i.e. "property", "Something about this class.").
     */
    public void addAnnotation (JCTree.JCClassDecl cdecl, Class<? extends Annotation> aclass,
                                String... argsVals);

    /**
     * Adds an annotation to the supplied method AST node.
     *
     * @param mdecl the method onto which to add the annotation.
     * @param aclass the class of the annotation to be added.
     * @param argsVals an array of strings representing the annotation field names and values
     * (i.e. "property", "Does not modify object state.").
     */
    public void addAnnotation (JCTree.JCMethodDecl mdecl, Class<? extends Annotation> aclass,
                                String... argsVals);

    /**
     * Adds an annotation to the supplied method parameter AST node.
     *
     * @param pdecl the method parameter onto which to add the annotation.
     * @param aclass the class of the annotation to be added.
     * @param argsVals an array of strings representing the annotation field names and values
     * (i.e. "property", "Must not be null.").
     */
    public void addAnnotation (JCTree.JCVariableDecl pdecl, Class<? extends Annotation> aclass,
                               String... argsVals);

    /**
     * Emits an informational log message.
     */
    public void info (String message);

    /**
     * Emits a warning log message.
     */
    public void warn (String message);
}
