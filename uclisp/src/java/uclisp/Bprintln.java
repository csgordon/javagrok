//
// Bprintln - calls print and then outputs a newline

package uclisp;

public class Bprintln extends Bprint
{
    //
    // Bprintln public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        Object v = super.evaluate(interp, args);
        _out.println("");
        return v;
    }
}
