//
// Bgt - built in to perform comparison

package uclisp;

public class Bgt extends Function
{
    //
    // Bgt public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            int l = (Integer)interp.evaluateSExp(args.car);
            int r = (Integer)interp.evaluateSExp(args.cdr.car);
            return l > r ? 1 : 0;

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for > expression.", args);
        }
    }

    public int numArguments ()
    {
        return 2;
    }
}
