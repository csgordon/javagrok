//
// Bdiv - built in to perform division

package uclisp;

public class Bdiv extends Function
{
    //
    // Bdiv public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        try {
            Integer l = (Integer)interp.evaluateSExp(sexp.elementAt(0));
            Integer r = (Integer)interp.evaluateSExp(sexp.elementAt(1));

            if (l.intValue() == 0)
                throw new RunTimeException("Division by zero.", sexp);

            return new Integer(r.intValue()/l.intValue());

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "subtraction expression.", sexp);
        }
    }

    public int numArguments ()
    {
        return 2;
    }
}
