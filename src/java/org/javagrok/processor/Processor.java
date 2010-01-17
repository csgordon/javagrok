//
// $Id$

package org.javagrok.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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

import org.javagrok.analysis.AnalysisContext;
import org.javagrok.analysis.Analyzer;

/**
 * The main entry point for the javac analysis executing annotation processor.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor
{
    @Override // from AbstractProcessor
    public void init (final ProcessingEnvironment procEnv)
    {
        super.init(procEnv);

        _ctx = new AnalysisContext() {
            public void info (String message) {
                procEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
            }
            public void warn (String message) {
                procEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
            }
        };

        // locate our analyses via their META-INF/services declarations
        for (String aname : findAnalyses(procEnv)) {
            try {
                _analyzers.add((Analyzer)Class.forName(aname).newInstance());
            } catch (Exception e) {
                _ctx.warn("Failed to instantiate analyzer '" + aname + "': " + e);
            }
        }

        // now initialize our analyzers
        for (Analyzer a : _analyzers) {
            try {
                a.init(_ctx);
            } catch (Exception e) {
                _ctx.warn("Failed to initialize analyzer '" + a + "': " + e);
            }                
        }

        procEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "JavaGrok running");
    }

    @Override // from AbstractProcessor
    public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        // run our analyzers in turn
        for (Analyzer a : _analyzers) {
            try {
                a.process(_ctx, roundEnv.getRootElements());
            } catch (Exception e) {
                _ctx.warn("Analyzer failed in process() '" + a + "': " + e);
            }
        }
        return false;
    }

    protected List<String> findAnalyses (ProcessingEnvironment procEnv)
    {
        List<String> provs = new ArrayList<String>();
        try {
            Enumeration<URL> svcurls =
                getClass().getClassLoader().getResources("META-INF/services/" + SERVICE_NAME);
            while (svcurls.hasMoreElements()) {
                readProviders(provs, svcurls.nextElement());
            }
        } catch (IOException ioe) {
            procEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING, "JavaGrok failed to enumerate analyzers: " + ioe);
        }
        return provs;
    }

    protected void readProviders (List<String> provs, URL svcurl)
        throws IOException
    {
        BufferedReader bin = new BufferedReader(new InputStreamReader(svcurl.openStream()));
        try {
            String prov;
            while ((prov = bin.readLine()) != null) {
                provs.add(prov);
            }
        } finally {
            bin.close();
        }
    }

    protected AnalysisContext _ctx;
    protected List<Analyzer> _analyzers = new ArrayList<Analyzer>();

    protected static final String SERVICE_NAME = "org.javagrok.analysis.Analyzer";
}
