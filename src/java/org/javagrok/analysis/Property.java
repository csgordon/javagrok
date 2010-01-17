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
 * TODO: maybe we should split this up into ArgProperty, MethodProperty, and ClassProperty?
 * (FieldProperty?)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Property
{
    /** The name of the method parameter to which this property applies, if any. */
    public String param () default "";

    /** The text of the property. */
    public String property ();
}
