//
// Bsetq - a builtin function to rebind a name to a value

package uclisp;

public class Bsetq extends Function
{
    //
    // Bsetq public member functions

    public Object evaluate (Interpreter interp, List args)
        throws RunTimeException
    {
        Object rhs = null;

        for (int i = 0; i < args.size(); i += 2) {
            try {
                String lhs = ((Name)args.elementAt(i)).toString();
                Object orhs = interp.env.get(lhs);
                rhs = interp.evaluateSExp(args.elementAt(i+1));

                if ((orhs != null) &&
                    (orhs.getClass() != rhs.getClass())) {
                    throw new RunTimeException("Type mismatch error.", args);
                }

                interp.env.put(lhs, rhs);

            } catch (ClassCastException cce) {
                throw new RunTimeException(cce.toString(), args);
            }
        }

        return rhs;
    }

    public void verifyArguments (List args) throws RunTimeException
    {
        if (args.size() < 2)
            throw new RunTimeException("Too few arguments to setq.", args);
        if (args.size() % 2 != 0)
            throw new RunTimeException("setq requires variable " +
                                       "value pairs.", args);
    }
}
