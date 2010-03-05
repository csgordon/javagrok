//
// Bwhile - built in to perform if comparison

package uclisp;

public class Bwhile extends Function
{
    //
    // Bwhile public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            Integer cond = (Integer)interp.evaluateSExp(args.car);
            while (cond.intValue() != 0) {
                interp.interpret(new Progn(args.cdr));
                cond = (Integer)interp.evaluateSExp(args.car);
            }
            return List.nil;

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "while expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2) {
            throw new RunTimeException("Incorrect number of arguments " +
                                       "to while.", args);
        }
    }
}
