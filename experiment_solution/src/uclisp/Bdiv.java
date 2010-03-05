//
// Bdiv - built in to perform division

package uclisp;

import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.Retained;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.UniqueReturn;
import java.util.Vector;

public class Bdiv extends Function
{
    //
    // Bdiv public member functions

    @LentThis
    @UniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException when (l.intValue() == 0)<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    public Object evaluate (@NotRetained Interpreter interp, @Retained Vector sexp)
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

    @LentThis
    public int numArguments ()
    {
        return 2;
    }
}
