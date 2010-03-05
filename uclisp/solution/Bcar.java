//
// Bcar - a builtin function to return the head of a list

package uclisp;

public class Bcar extends Function
{
    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        Object list = interp.evaluateSExp(args.car);
        if (list == List.nil) {
            return List.nil;
        } else if (!(list instanceof List)) {
            fail("Non-list passed to car", args);
        }
        return ((List)list).car;
    }

    public int numArguments ()
    {
        return 1;
    }
}
