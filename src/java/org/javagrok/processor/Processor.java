//
// $Id$

package org.javagrok.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.tools.Diagnostic;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * The main entry point for the javac annotation processor.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor
{
    @Override // from AbstractProcessor
    public void init (ProcessingEnvironment procenv)
    {
        super.init(procenv);

//         if (!(procenv instanceof JavacProcessingEnvironment)) {
//             procenv.getMessager().printMessage(
//                 Diagnostic.Kind.WARNING, "JavaGrok requires javac v1.6+.");
//             return;
//         }

//         Context ctx = ((JavacProcessingEnvironment)procenv).getContext();
//         _trees = Trees.instance(procenv);
        _procenv = procenv;

        procenv.getMessager().printMessage(
            Diagnostic.Kind.NOTE, "JavaGrok running [vers=" + procenv.getSourceVersion() + "]");
    }

    @Override // from AbstractProcessor
    public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
//         if (_trees == null) {
//             return false;
//         }

        for (Element elem : roundEnv.getRootElements()) {
            System.out.println("Got " + elem);
//             final JCCompilationUnit unit = toUnit(elem);

//             // we only want to operate on files being compiled from source; if they're already
//             // classfiles then we've already run or we're looking at a library class
//             if (unit.sourcefile.getKind() != JavaFileObject.Kind.SOURCE) {
//                 System.err.println("Skipping non-source-file " + unit.sourcefile);
//                 continue;
//             }
        }

        return false;
    }

    protected ProcessingEnvironment _procenv;
//     protected Trees _trees;
}
