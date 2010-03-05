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
            Integer cond = (Integer)interp.evaluateSExp(args.car);
            if (cond.intValue() != 0) {
                return interp.evaluateSExp(args.elementAt(1));

            } else if (args.size() == 3) {
                return interp.evaluateSExp(args.elementAt(2));

            } else {
                return Nil.nil;
            }

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "conditional expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if ((args.size() != 2) && (args.size() != 3))
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to if.", args);
    }
}
