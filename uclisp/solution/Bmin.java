//
// Bmin - a builtin function to return the least of its arguments

package uclisp;

public class Bmin extends Function
{
    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            int min = Integer.MAX_VALUE;
            for (List l = args; !l.isNil(); l = l.cdr) {
                min = Math.min(min, (Integer)interp.evaluateSExp(l.car));
            }
            return min;

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for min expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            fail("Incorrect number of arguments to min.", args);
    }
}
