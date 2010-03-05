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

            for (int i = 0; i < args.size(); i++) {
                Integer ival = (Integer)interp.evaluateSExp(args.elementAt(i));
                prod *= ival.intValue();
            }

            return new Integer(prod);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "multiplication expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to mul.", args);
    }
}
