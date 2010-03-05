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
            Integer v = (Integer)interp.evaluateSExp(args.elementAt(0));
            int result = v.intValue();

            for (int i = 1; i < args.size(); i++) {
                Integer ival = (Integer)interp.evaluateSExp(args.elementAt(i));
                result -= ival.intValue();
            }

            return new Integer(result);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "subtraction expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to sub.", args);
    }
}
