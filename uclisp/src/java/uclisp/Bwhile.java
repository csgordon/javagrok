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
            int cond = (Integer)interp.evaluateSExp(args.car);
            while (cond != 0) {
                interp.interpret(new Progn(args.cdr));
                cond = (Integer)interp.evaluateSExp(args.car);
            }
            return List.nil;

        } catch (ClassCastException cce) {
            return fail("Non-integer type used for while expression.", args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2) {
            fail("while requires at least two arguments.", args);
        }
    }
}
