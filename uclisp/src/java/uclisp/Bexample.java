//
// Bexample - built in example

package uclisp;

public class Bexample extends Function
{
    //
    // Bexample public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        return List.nil;
    }

    public int numArguments ()
    {
        return 0;
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        super.verifyArguments(args);
    }
}
