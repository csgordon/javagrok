//
// Badd - built in to perform addition

package uclisp;

public class Badd extends Function
{
    //
    // Badd public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            int sum = 0;
            for (List l = args; !l.isNil(); l = l.cdr) {
                int ival = (Integer)interp.evaluateSExp(l.car);
                sum += ival;
            }
            return sum;

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for add expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            fail("Incorrect number of arguments to add.", args);
    }
}
