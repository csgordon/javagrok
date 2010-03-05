//
// Bdefun - built in to define functions

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.UniqueReturn;
import java.util.Hashtable;
import java.util.Vector;

public class Bdefun extends Function
{
    //
    // Bdefun public member functions

    @LentThis
    @UniqueReturn
    @ExceptionProperty(throwsWhen="<br>RunTimeException when (!(args.elementAt(i) instanceof Name))<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    public Object evaluate (@NotRetained Interpreter interp, @Retained Vector sexp)
        throws RunTimeException
    {
        try {
            String name = ((Name)sexp.firstElement()).toString();
            Vector args = (Vector)sexp.elementAt(1);

            // first check the args for validity. should be a list of vars
            for (int i = 0; i < args.size(); i++) {
                if (!(args.elementAt(i) instanceof Name)) {
                    throw new RunTimeException("Argument #" + i +
                                               " of defun not valid.",
                                               args.elementAt(i));
                }
            }

            // create a new user function with this stuff
            UserFunction fn = new UserFunction(name, args,
                                               Interpreter.ccdr(sexp));
            // System.out.println("Binding function: " + name);
            interp.env.put(name, fn);

            return new Nil();

        } catch (ClassCastException cce) {
            throw new RunTimeException(cce.toString(), sexp);
        }
    }

    @LentThis
    @ExceptionProperty(throwsWhen="<br>RunTimeException always<br>RunTimeException always<br>RunTimeException always", exceptionsThrown="explicitly throws RunTimeException")
    public void verifyArguments (@Retained Vector sexp) throws RunTimeException
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
