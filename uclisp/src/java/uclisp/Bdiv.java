//
// Bdiv - built in to perform division

package uclisp;

public class Bdiv extends Function
{
    //
    // Bdiv public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            int l = (Integer)interp.evaluateSExp(args.car);
            int r = (Integer)interp.evaluateSExp(args.cdr.car);
            if (l == 0) fail("Division by zero.", args);
            return r/l;

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for div expression.", args);
        }
    }

    public int numArguments ()
    {
        return 2;
    }
}
