//
// Bwhile - built in to perform if comparison

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.UniqueReturn;
import java.util.Vector;

public class Bwhile extends Function
{
    //
    // Bwhile public member functions

    @LentThis
    @UniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    public Object evaluate (@NotRetained Interpreter interp, @Retained Vector sexp)
        throws RunTimeException
    {
        try {
            Vector body = Interpreter.cdr(sexp);
            Integer cond = (Integer)interp.evaluateSExp(sexp.firstElement());

            while (cond.intValue() != 0) {
                interp.interpret(body);
                cond = (Integer)interp.evaluateSExp(sexp.firstElement());
            }

            return new Nil();

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "while expression.", sexp);
        }
    }

    @LentThis
    @ExceptionProperty(throwsWhen="<br>RunTimeException when (sexp.size() < 2)", exceptionsThrown="explicitly throws RunTimeException")
    public void verifyArguments (@Retained Vector sexp) throws RunTimeException
    {
        if (sexp.size() < 2)
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to while.", sexp);
    }
}
