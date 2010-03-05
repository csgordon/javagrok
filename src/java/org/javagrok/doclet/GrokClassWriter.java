//
// $Id$

package org.javagrok.doclet;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.tools.doclets.formats.html.ClassWriterImpl;
import com.sun.tools.doclets.internal.toolkit.ClassWriter;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;

/**
 * Extends the default {@link ClassWriter} implementation with hooks to emit JavaGrok annotation
 * documentation.
 */
public class GrokClassWriter extends ClassWriterImpl
{
    public GrokClassWriter (ClassDoc classDoc, ClassDoc prev, ClassDoc next, ClassTree tree)
        throws Exception
    {
        super(classDoc, prev, next, tree);
    }

    @Override public void writeClassDescription ()
    {
        super.writeClassDescription();

        for (AnnotationDesc desc : classDoc.annotations()) {
            print("Annotation: " + desc + "<br/>");
        }
    }
}
