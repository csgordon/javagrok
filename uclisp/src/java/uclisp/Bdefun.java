//
// Bdefun - built in to define functions

package uclisp;

public class Bdefun extends Function
{
    //
    // Bdefun public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        try {
            String name = ((Name)args.car).toString();
            List alist = ((Apply)args.cdr.car).sexp;
            List body = args.cdr.cdr;

            // first check the args for validity. should be a list of vars
            for (int i = 0; i < alist.size(); i++) {
                if (!(alist.elementAt(i) instanceof Name)) {
                    fail("Argument #" + i + " of defun not valid.", args);
                }
            }

            // create a new user function with this stuff
            interp.env.put(name, new UserFunction(name, alist, body));

            return List.nil;

        } catch (ClassCastException cce) {
            throw new RunTimeException(cce.toString(), args);
        }
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        switch (args.size()) {
        case 0: fail("Missing function name for defun.", args); break;
        case 1: fail("Missing argument list for defun.", args); break;
        case 2: fail("Missing function body for defun.", args); break;
        }
    }
}
