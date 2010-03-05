//
// Bprogn - evaluates each element of a list and returns the final element

package uclisp;

public class Bprogn extends Function
{
    //
    // Bprogn public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        Object rv = new Nil();
        while (!sexp.isEmpty()) {
            rv = interp.evaluateSExp(sexp.car);
            sexp = sexp.cdr;
        }
        return rv;
    }
}
