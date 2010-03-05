//
// Bprint - a builtin function to print out an sexp

package uclisp;

import java.io.PrintStream;

public class Bprint extends Function
{
    //
    // Bprint public static member functions

    public static void setOutputStream (PrintStream out)
    {
        _out = out;
    }

    //
    // Bprint public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        StringBuilder buf = new StringBuilder();
        for (List l = args; !l.isNil(); l = l.cdr) {
            formatValue(interp, l.car, buf);
        }
        _out.print(buf.toString());
        _out.flush();
        return Nil.nil;
    }

    //
    // Bprint protected member functions

    protected void formatValue (Interpreter interp, Object v, StringBuilder buf)
        throws RunTimeException
    {
        if ((v instanceof String) ||
            (v instanceof Integer)) {
            buf.append(v);

        } else if (v instanceof Nil) {
            buf.append("nil");

        } else if (v instanceof List) {
            buf.append("(");
            for (List l = (List)v; !l.isNil(); l = l.cdr) {
                if (l != v) {
                    buf.append(" ");
                }
                formatValue(interp, l.car, buf);
            }
            buf.append(")");

        } else if (v instanceof Name) {
            formatValue(interp, interp.evaluateSExp(v), buf);

        } else if (v instanceof Apply) {
            formatValue(interp, interp.evaluateSExp(v), buf);

        } else {
            throw new RunTimeException("print: invalid type: " +
                                       v.getClass().getName(), v);
        }
    }

    //
    // Bprint protected static data members

    protected static PrintStream _out = System.out;
}
