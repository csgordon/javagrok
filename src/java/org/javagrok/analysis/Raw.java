//
// $Id$

package org.javagrok.analysis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Communicates that a method is called on an object before it is fully initialized (called from
 * its constructor). Not sure what it means when it's on a variable declaration.
 */
@Retention(RetentionPolicy.CLASS)
public @interface Raw
{
}
