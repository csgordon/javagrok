//
// Bsub - built in to perform subtraction

package uclisp;

public class Bsub extends Function
{
    //
    // Bsub public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            int result = (Integer)interp.evaluateSExp(args.car);
            for (List l = args.cdr; !l.isNil(); l = l.cdr) {
                result -= (Integer)interp.evaluateSExp(l.car);
            }
            return result;

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for subtraction expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            fail("Incorrect number of arguments to sub.", args);
    }
}
