//
// Bwhile - built in to perform if comparison

package uclisp;

public class Bwhile extends Function
{
    //
    // Bwhile public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        try {
            Integer cond = (Integer)interp.evaluateSExp(sexp.car);
            while (cond.intValue() != 0) {
                interp.interpret(new Progn(sexp.cdr));
                cond = (Integer)interp.evaluateSExp(sexp.car);
            }
            return new Nil();

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "while expression.", sexp);
        }
    }

    public void verifyArguments (List sexp) throws RunTimeException
    {
        if (sexp.size() < 2) {
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to while.", sexp);
        }
    }
}
