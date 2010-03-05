//
// Bprintln - calls print and then outputs a newline

package uclisp;

import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.UniqueReturn;
import org.javagrok.analysis.LentThis;
import java.io.PrintStream;

import java.util.Vector;

public class Bprintln extends Bprint
{
    //
    // Bprintln public member functions

    @LentThis
    @UniqueReturn
    public Object evaluate (@NotRetained Interpreter interp, @NotRetained Vector sexp)
        throws RunTimeException
    {
        Object v = super.evaluate(interp, sexp);
        _out.println("");
        return v;
    }
}
