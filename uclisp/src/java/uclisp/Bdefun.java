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
                    throw new RunTimeException("Argument #" + i +
                                               " of defun not valid.",
                                               alist.elementAt(i));
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
        case 0:
            throw new RunTimeException("Missing function name for defun.",
                                       args);

        case 1:
            throw new RunTimeException("Missing argument list for defun.",
                                       args);

        case 2:
            throw new RunTimeException("Missing function body for defun.",
                                       args);
        }
    }
}
