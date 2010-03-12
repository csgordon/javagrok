//
// Function -

package uclisp;

public class Function
{
    //
    // Function public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        throw new UnsupportedOperationException();
    }

    public int numArguments ()
    {
        return -1;
    }

    public String name ()
    {
        return getClass().getName().substring(1);
    }

    public void verifyArguments (List args)
        throws RunTimeException
    {
        if (numArguments() >= 0 && numArguments() != args.size()) {
            fail("Called " + numArguments() + " arg function with " +
                 args.size() + " args", args);
        }
    }

    protected Object fail (String message, List args)
        throws RunTimeException
    {
        throw new RunTimeException(message, args.cons(new Name(name())));
    }
}
