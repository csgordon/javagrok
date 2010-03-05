//
// Beq - built in to perform equality comparison

package uclisp;

public class Beq extends Function
{
    //
    // Beq public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            int val =
                ((Integer)interp.evaluateSExp(args.elementAt(0))).intValue();

            for (int i = 1; i < args.size(); i++) {
                Integer ival = (Integer)interp.evaluateSExp(args.elementAt(i));
                if (ival.intValue() != val) return new Integer(0);
            }

            return new Integer(1);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "equality expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to eq.", args);
    }
}
