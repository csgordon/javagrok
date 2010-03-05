//
// Bmul - built in to perform multiplication

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.UniqueReturn;
import java.util.Vector;

public class Bmul extends Function
{
    //
    // Bmul public member functions

    @LentThis
    @UniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    public Object evaluate (@NotRetained Interpreter interp, @Retained Vector sexp)
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

    @LentThis
    @ExceptionProperty(throwsWhen="<br>RunTimeException when (sexp.size() < 2)", exceptionsThrown="explicitly throws RunTimeException")
    public void verifyArguments (@Retained Vector sexp) throws RunTimeException
    {
        if (sexp.size() < 2)
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to mul.", sexp);
    }
}
