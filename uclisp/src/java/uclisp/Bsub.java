//
// Bsub - built in to perform subtraction

package uclisp;

public class Bsub extends Function
{
    //
    // Bsub public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        try {
            Integer v = (Integer)interp.evaluateSExp(sexp.elementAt(0));
            int result = v.intValue();

            for (int i = 1; i < sexp.size(); i++) {
                Integer ival = (Integer)interp.evaluateSExp(sexp.elementAt(i));
                result -= ival.intValue();
            }

            return new Integer(result);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "subtraction expression.", sexp);
        }
    }

    public void verifyArguments (List sexp) throws RunTimeException
    {
        if (sexp.size() < 2)
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to sub.", sexp);
    }
}
