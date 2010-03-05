//
// Bif - built in to perform if comparison

package uclisp;

public class Bif extends Function
{
    //
    // Bif public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            int cond = (Integer)interp.evaluateSExp(args.car);
            if (cond != 0) {
                return interp.evaluateSExp(args.elementAt(1));
            } else if (args.size() == 3) {
                return interp.evaluateSExp(args.elementAt(2));
            } else {
                return List.nil;
            }

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for if expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if ((args.size() != 2) && (args.size() != 3))
            fail("Incorrect number of arguments to if.", args);
    }
}
