//
// Interpreter -

package uclisp;

import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class Interpreter
{
    //
    // Interpreter public data members

    public Map<Object,Object> env = new HashMap<Object,Object>();

    //
    // Interpreter public static member functions

    public static void main (String[] args)
    {
        if (args.length != 1) {
            System.out.println("Usage: java uclisp.Interpreter source_file");
            System.exit(255);
            return;
        }

        try {
            Interpreter interp = new Interpreter();
            Parser parser = new Parser(new FileReader(args[0]));
            interp.interpret(parser.parse());

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

    //
    // Interpreter public member functions

    public Object interpret (Progn progn) throws RunTimeException
    {
        Object result = List.nil;
        for (List l = progn.sexps; !l.isNil(); l = l.cdr) {
            result = evaluateSExp(l.car);
        }
        return result;
    }

    //
    // Interpreter protected member functions

    Object evaluateSExp (Object sexp) throws RunTimeException
    {
        if ((sexp instanceof Integer) ||
            (sexp instanceof String) ||
            (sexp instanceof Function) ||
            (sexp instanceof List)) {
            return sexp;

        } else if (sexp instanceof Name) {
            return getValue(sexp.toString());

        } else if (sexp instanceof Apply) {
            return evaluateCall(((Apply)sexp).sexp);

        } else {
            throw new RunTimeException("Unknown sexp type: " +
                                       sexp.getClass().getName(), sexp);
        }
    }

    Object getValue (Object sexp) throws RunTimeException
    {
        Object val = env.get(sexp);
        if (val != null) return val;
        return List.nil;
    }

    Object evaluateCall (List sexp) throws RunTimeException
    {
        if (sexp.isNil()) {
            throw new RunTimeException("Evaluation of empty sexp.", sexp);
        }

        try {
            String nm = ((Name)sexp.car).toString();
            Function fn = (Function)env.get(nm);

            if (fn == null) {
                try {
                    Class<?> bic = Class.forName("uclisp.B" + nm);
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

            List args = sexp.cdr;
            fn.verifyArguments(args);
            Object rv = fn.evaluate(this, args);
            if (rv == null) {
                throw new RunTimeException("Function must return nil, not null.", sexp);
            }
            return rv;

        } catch (ClassCastException cce) {
            cce.printStackTrace();
            throw new RunTimeException(cce.toString(), sexp);
        }
    }
}
