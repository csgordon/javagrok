//
// Bmul - built in to perform multiplication

package uclisp;

public class Bmul extends Function
{
    //
    // Bmul public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        try {
            int prod = 1;

            for (int i = 0; i < sexp.size(); i++) {
                Integer ival = (Integer)interp.evaluateSExp(sexp.elementAt(i));
                prod *= ival.intValue();
            }

            return new Integer(prod);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "multiplication expression.", sexp);
        }
    }

    public void verifyArguments (List sexp) throws RunTimeException
    {
        if (sexp.size() < 2)
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to mul.", sexp);
    }
}
