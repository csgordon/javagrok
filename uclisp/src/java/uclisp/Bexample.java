//
// Bexample - built in example

package uclisp;

public class Bexample extends Function
{
    //
    // Bexample public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        return new Nil();
    }

    public int numArguments ()
    {
        return 0;
    }

    public void verifyArguments (List sexp) throws RunTimeException
    {
        super.verifyArguments(sexp);
    }
}
