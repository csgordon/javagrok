//
// Bsetq - a builtin function to setq out an sexp

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.NonUniqueReturn;
import java.util.Vector;

public class Bsetq extends Function
{
    //
    // Bsetq public member functions

    @LentThis
    @NonUniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException when ((orhs != null) && (orhs.getClass() != rhs.getClass()))<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    public Object evaluate (@NotRetained Interpreter interp, @Retained Vector sexp)
        throws RunTimeException
    {
        Object rhs = null;

        for (int i = 0; i < sexp.size(); i += 2) {
            try {
                String lhs = ((Name)sexp.elementAt(i)).toString();
                Object orhs = interp.env.get(lhs);
                rhs = interp.evaluateSExp(sexp.elementAt(i+1));

                if ((orhs != null) &&
                    (orhs.getClass() != rhs.getClass())) {
                    throw new RunTimeException("Type mismatch error.", sexp);
                }

                interp.env.put(lhs, rhs);

            } catch (ClassCastException cce) {
                throw new RunTimeException(cce.toString(), sexp);
            }
        }

        return rhs;
    }

    @LentThis
    @ExceptionProperty(throwsWhen="<br>RunTimeException when (sexp.size() < 2)<br>RunTimeException when (sexp.size() % 2 != 0)", exceptionsThrown="explicitly throws RunTimeException")
    public void verifyArguments (@Retained Vector sexp) throws RunTimeException
    {
        if (sexp.size() < 2)
            throw new RunTimeException("Too few arguments to setq.", sexp);
        if (sexp.size() % 2 != 0)
            throw new RunTimeException("setq requires variable " +
                                       "value pairs.", sexp);
    }
}
