//
// Bexample - built in example

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.UniqueReturn;
import java.util.Vector;

public class Bexample extends Function
{
    //
    // Bexample public member functions

    @LentThis
    @UniqueReturn
    public Object evaluate (@NotRetained Interpreter interp, @NotRetained Vector sexp)
        throws RunTimeException
    {
        return new Nil();
    }

    @LentThis
    public int numArguments ()
    {
        return 0;
    }

    @LentThis
    public void verifyArguments (@Retained Vector sexp) throws RunTimeException
    {
        super.verifyArguments(sexp);
    }
}
