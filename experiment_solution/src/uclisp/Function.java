//
// Function -

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.NonUniqueReturn;
import org.javagrok.analysis.NotRetained;
import java.util.Vector;

public class Function
{
    //
    // Function public member functions

    @LentThis
    @NonUniqueReturn
    public Object evaluate (@NotRetained Interpreter interp, @Retained Vector sexp)
        throws RunTimeException
    {
        return null;
    }

    @LentThis
    public int numArguments ()
    {
        return -1;
    }

    @LentThis
    @NonUniqueReturn
    public String name ()
    {
        return getClass().getName().substring(1);
    }

    @LentThis
    @ExceptionProperty(throwsWhen="<br>RunTimeException when (numArguments() != sexp.size())", exceptionsThrown="explicitly throws RunTimeException")
    public void verifyArguments (@Retained Vector sexp)
        throws RunTimeException
    {
        if (numArguments() == -1) return;
        if (numArguments() != sexp.size())
            throw new RunTimeException("Illegal call to " +
                                       numArguments() + " arg function [" +
                                       name() + "] with " +
                                       sexp.size() + " args", sexp);
    }
}
