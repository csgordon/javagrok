//
// $Id$

package org.javagrok.analysis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reports exceptions thrown by a method, and under what circumstances.
 *
 * TODO: maybe we should split this up into ParamProperty, MethodProperty, FieldProperty and
 * ClassProperty?
 */
@Documented
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface ExceptionProperty
{
    /** The text of the property. */
    public String throwsWhen ();
    public String exceptionsThrown ();

    //public String methodsCalled ();

}
