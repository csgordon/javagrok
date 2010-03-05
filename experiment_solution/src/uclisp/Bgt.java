//
// Bgt - built in to perform comparison

package uclisp;

import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.Retained;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.UniqueReturn;
import java.util.Vector;

public class Bgt extends Function
{
    //
    // Bgt public member functions

    @LentThis
    @UniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    public Object evaluate (@NotRetained Interpreter interp, @Retained Vector sexp)
        throws RunTimeException
    {
        try {
            Integer l = (Integer)interp.evaluateSExp(sexp.elementAt(0));
            Integer r = (Integer)interp.evaluateSExp(sexp.elementAt(1));

            return new Integer(l.intValue() > r.intValue() ? 1 : 0);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "comparison expression.", sexp);
        }
    }

    @LentThis
    public int numArguments ()
    {
        return 2;
    }
}
