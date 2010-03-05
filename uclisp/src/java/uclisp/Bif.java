//
// Bif - built in to perform if comparison

package uclisp;

public class Bif extends Function
{
    //
    // Bif public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        try {
            Integer cond = (Integer)interp.evaluateSExp(sexp.car);
            if (cond.intValue() != 0) {
                return interp.evaluateSExp(sexp.elementAt(1));

            } else if (sexp.size() == 3) {
                return interp.evaluateSExp(sexp.elementAt(2));

            } else {
                return new Nil();
            }

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "conditional expression.", sexp);
        }
    }

    public void verifyArguments (List sexp) throws RunTimeException
    {
        if ((sexp.size() != 2) && (sexp.size() != 3))
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to if.", sexp);
    }
}
