//
// Bcons - a builtin function to construct a new list

package uclisp;

public class Bcons extends Function
{
    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        Object car = interp.evaluateSExp(args.car);
        Object cdr = interp.evaluateSExp(args.cdr.car);
        if (!(cdr instanceof List)) {
            fail("Non-list passed as second argument to cdr", args);
        }
        return ((List)cdr).cons(car);
    }

    public int numArguments ()
    {
        return 2;
    }
}
