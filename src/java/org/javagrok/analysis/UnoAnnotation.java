//
// $Id$

package org.javagrok.analysis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reports a property of a class or method deduced by an analysis.
 *
 * TODO: maybe we should split this up into ParamProperty, MethodProperty, FieldProperty and
 * ClassProperty?
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface UnoAnnotation
{
    /** The text of the property. */
    public String property ();
}
