//
// Bgte - built in to perform comparison

package uclisp;

public class Bgte extends Function
{
    //
    // Bgte public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            Integer l = (Integer)interp.evaluateSExp(args.elementAt(0));
            Integer r = (Integer)interp.evaluateSExp(args.elementAt(1));

            return new Integer(l.intValue() >= r.intValue() ? 1 : 0);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "comparison expression.", args);
        }
    }

    public int numArguments ()
    {
        return 2;
    }
}
