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

            for (int i = 0; i < args.size(); i++) {
                Integer ival = (Integer)interp.evaluateSExp(args.elementAt(i));
                sum += ival.intValue();
            }

            return new Integer(sum);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "addition expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to add.", args);
    }
}
