//
// Bmul - built in to perform multiplication

package uclisp;

public class Bmul extends Function
{
    //
    // Bmul public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            int prod = 1;
            for (List l = args; !l.isNil(); l = l.cdr) {
                prod *= (Integer)interp.evaluateSExp(l.car);
            }
            return prod;

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for mul expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            fail("Incorrect number of arguments to mul.", args);
    }
}
