//
// $Id$

package org.javagrok.analysis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Communicates that a variable declaration must not hold null values.
 */
@Retention(RetentionPolicy.CLASS)
public @interface NonNull
{
}
