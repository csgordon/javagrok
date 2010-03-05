//
// Bdefun - built in to define functions

package uclisp;

public class Bdefun extends Function
{
    //
    // Bdefun public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        try {
            String name = ((Name)sexp.car).toString();
            List args = (List)sexp.cdr.car;
            List body = sexp.cdr.cdr;

            // first check the args for validity. should be a list of vars
            for (int i = 0; i < args.size(); i++) {
                if (!(args.elementAt(i) instanceof Name)) {
                    throw new RunTimeException("Argument #" + i +
                                               " of defun not valid.",
                                               args.elementAt(i));
                }
            }

            // create a new user function with this stuff
            interp.env.put(name, new UserFunction(name, args, body));

            return new Nil();

        } catch (ClassCastException cce) {
            throw new RunTimeException(cce.toString(), sexp);
        }
    }

    public void verifyArguments (List sexp) throws RunTimeException
    {
        switch (sexp.size()) {
        case 0:
            throw new RunTimeException("Missing function name for defun.",
                                       sexp);

        case 1:
            throw new RunTimeException("Missing argument list for defun.",
                                       sexp);

        case 2:
            throw new RunTimeException("Missing function body for defun.",
                                       sexp);
        }
    }
}
