//
// Bprint - a builtin function to print out an sexp

package uclisp;

import java.io.PrintStream;

public class Bprint extends Function
{
    //
    // Bprint public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        return evaluate(interp, args, "");
    }

    public Object evaluate (Interpreter interp, List args, String sep)
        throws RunTimeException
    {
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                _out.print(sep);
            }
            printValue(interp, args.elementAt(i));
        }
        _out.flush();
        return Nil.nil;
    }

    //
    // Bprint protected member functions

    public void printValue (Interpreter interp, Object v)
        throws RunTimeException
    {
        if ((v instanceof String) ||
            (v instanceof Integer)) {
            _out.print(v);

        } else if (v instanceof Nil) {
            _out.print("nil");

        } else if (v instanceof List) {
            _out.print("(");
            evaluate(interp, (List)v, " ");
            _out.print(")");

        } else if (v instanceof Name) {
            printValue(interp, interp.evaluateSExp(v));

        } else if (v instanceof Apply) {
            printValue(interp, interp.evaluateSExp(v));

        } else {
            throw new RunTimeException("print: invalid type: " +
                                       v.getClass().getName(), v);
        }
    }

    //
    // Bprint public static member functions

    public static void setOutputStream (PrintStream out)
    {
        _out = out;
    }

    //
    // Bprint protected static data members

    static PrintStream _out = System.out;
}
