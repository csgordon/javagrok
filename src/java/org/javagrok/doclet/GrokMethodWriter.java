//
// $Id$

package org.javagrok.doclet;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Type;
import com.sun.tools.doclets.formats.html.MethodWriterImpl;
import com.sun.tools.doclets.formats.html.SubWriterHolderWriter;
import com.sun.tools.doclets.internal.toolkit.ClassWriter;
import com.sun.tools.doclets.internal.toolkit.MethodWriter;

/**
 * Extends the default {@link MethodWriter} implementation with hooks to emit JavaGrok annotation
 * documentation.
 */
public class GrokMethodWriter extends MethodWriterImpl
{
    public GrokMethodWriter (ClassWriter writer)
    {
        super((SubWriterHolderWriter)writer, writer.getClassDoc());
        System.err.println("Making writer " + writer.getClassDoc().qualifiedName());
    }

    @Override // from MethodWriterImpl
    public void writeComments (Type holder, MethodDoc method)
    {
        super.writeComments(holder, method);

        for (AnnotationDesc desc : method.annotations()) {
            writer.print("Annotation: " + desc);
        }
    }
}
