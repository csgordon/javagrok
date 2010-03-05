//
// Bif - built in to perform if comparison

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.NonUniqueReturn;
import java.util.Vector;

public class Bif extends Function
{
    //
    // Bif public member functions

    @LentThis
    @NonUniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    public Object evaluate (@NotRetained Interpreter interp, @Retained Vector sexp)
        throws RunTimeException
    {
        try {
            Integer cond = (Integer)interp.evaluateSExp(sexp.firstElement());

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

    @LentThis
    @ExceptionProperty(throwsWhen="<br>RunTimeException when ((sexp.size() != 2) && (sexp.size() != 3))", exceptionsThrown="explicitly throws RunTimeException")
    public void verifyArguments (@Retained Vector sexp) throws RunTimeException
    {
        if ((sexp.size() != 2) && (sexp.size() != 3))
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to if.", sexp);
    }
}
