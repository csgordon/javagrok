//
// Bprogn - evaluates each element of a list and returns the final element

package uclisp;

public class Bprogn extends Function
{
    //
    // Bprogn public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        Object rv = List.nil;
        while (!args.isNil()) {
            rv = interp.evaluateSExp(args.car);
            args = args.cdr;
        }
        return rv;
    }
}
