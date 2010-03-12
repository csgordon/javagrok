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
            // TODO: support list equality?
            int val = (Integer)interp.evaluateSExp(args.car);
            for (int i = 1; i < args.size(); i++) {
                int ival = (Integer)interp.evaluateSExp(args.elementAt(i));
                if (ival != val) return 0;
            }
            return 1;

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for eq expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            fail("Incorrect number of arguments to eq.", args);
    }
}
