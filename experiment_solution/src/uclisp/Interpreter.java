//
// Interpreter -

package uclisp;

import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.UniqueReturn;
import org.javagrok.analysis.Retained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.NonUniqueReturn;
import org.javagrok.analysis.LentThis;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

public class Interpreter
{
    //
    // Interpreter public data members

    Hashtable env = new Hashtable();

    //
    // Interpreter public member functions

    @LentThis
    @NonUniqueReturn
    public Object interpret (@Retained Object sexp) throws RunTimeException
    {
        // at this level we want to evaluate each sexp in a quoted list
        // intstead of simply returning the list like evaluateSExp would do
        if (sexp instanceof Vector) {

            Vector sexps = (Vector)sexp;
            Object result = null;

            Enumeration siter = sexps.elements();
            while (siter.hasMoreElements()) {
                result = evaluateSExp(siter.nextElement());
            }

            return result;

        } else {
            return evaluateSExp(sexp);
        }
    }

    //
    // Interpreter protected member functions

    @LentThis
    @NonUniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    Object evaluateSExp (@Retained Object sexp) throws RunTimeException
    {
        // we have to check for instanceof List before instanceof Vector
        if ((sexp instanceof Integer) ||
            (sexp instanceof String) ||
            (sexp instanceof Nil) ||
            (sexp instanceof Function) ||
            (sexp instanceof List)) {
            return sexp;
        }

        if (sexp instanceof Name) return getValue(sexp.toString());

        if (sexp instanceof Vector) return evaluateCall((Vector)sexp);

        throw new RunTimeException("Unknown sexp type: " +
                                   sexp.getClass().getName(), sexp);
    }

    @LentThis
    @NonUniqueReturn
    Object getValue (@Retained Object sexp) throws RunTimeException
    {
        Object val = env.get(sexp);
        if (val != null) return val;
        return new Nil();
    }

    @LentThis
    @NonUniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException when (fn == null)<br>RunTimeException when (fn == null)<br>RunTimeException when (fn == null)<br>RunTimeException always<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    Object evaluateCall (@Retained Vector sexp) throws RunTimeException
    {
        try {
            String nm = ((Name)sexp.firstElement()).toString();
            Function fn = (Function)env.get(nm);

            if (fn == null) {
                try {
                    Class bic = Class.forName("uclisp.B" + nm);
                    // create an instance of the builtin and insert it into
                    // the global environment
                    fn = (Function)bic.newInstance();
                    env.put(nm, fn);

                } catch (InstantiationException ie) {
                    throw new RunTimeException("Error instantiating " +
                                               "implementation class for " +
                                               "builtin function: " + nm,
                                               sexp);

                } catch (IllegalAccessException iae) {
                    throw new RunTimeException("Attempted use of class " +
                                               "that does not conform to " +
                                               "Function interface: " + nm,
                                               sexp);

                } catch (ClassNotFoundException cnfe) {
                    throw new RunTimeException("Reference to undefined " +
                                               "function: " + nm, sexp);
                }
            }

            Vector args = cdr(sexp);
            fn.verifyArguments(args);
            return fn.evaluate(this, args);

        } catch (NoSuchElementException nsee) {
            throw new RunTimeException("Evaluation of empty sexp.", sexp);

        } catch (ClassCastException cce) {
            throw new RunTimeException(cce.toString(), sexp);
        }
    }

    //
    // Interpreter public static member functions

    @UniqueReturn
    public static Vector cdr (@NotRetained Vector list)
    {
        return sublist(list, 1);
    }

    @UniqueReturn
    public static Vector ccdr (@NotRetained Vector list)
    {
        return sublist(list, 2);
    }

    @UniqueReturn
    public static Vector sublist (@NotRetained Vector list, int spos)
    {
        Vector nv = new Vector();
        for (int i = spos; i < list.size(); i++) {
            nv.addElement(list.elementAt(i));
        }
        return nv;
    }

    public static void main (@NotRetained String[] args)
    {
        if (args.length != 1) {
            System.out.println("Usage: java uclisp.Interpreter source_file");
            System.exit(255);
            return;
        }

        try {
            FileInputStream fin = new FileInputStream(args[0]);
            Parser parser = new Parser(fin);
            Object val = parser.parse();

            // System.out.println("Successfully parsed:");
            // System.out.println(val);

            Interpreter interp = new Interpreter();
            interp.interpret(val);

        } catch (RunTimeException rte) {
            System.err.println(rte);
            System.err.println("SExp: " + rte.sexp);

        } catch (ParseException pe) {
            System.err.println(pe);
            System.err.println("  Line: " + pe.lineNumber() +
                               " Token: " + pe.token());

        } catch (IOException e) {
            System.err.println("Error: " + e);
        }
    }
}
