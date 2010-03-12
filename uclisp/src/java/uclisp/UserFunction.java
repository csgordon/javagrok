//
// UserFunction - a user defined function interpreted at runtime

package uclisp;

import java.util.HashMap;
import java.util.Map;

public class UserFunction extends Function
{
    //
    // UserFunction public constructor

    public UserFunction (String name, List args, List progn)
    {
        _name = name;
        _args = args;
        _progn = new Progn(progn);
    }

    //
    // UserFunction public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        Map<String,Object> shadowed = new HashMap<String,Object>();

        for (int i = 0; i < _args.size(); i++) {
            String name = ((Name)_args.elementAt(i)).toString();
            Object val = sexp.elementAt(i);

            // shadow this variable
            Object oval = interp.env.get(name);
            if (oval != null) shadowed.put(name, oval);
            else shadowed.put(name, NO_VAL);

            // evaluate the argument and bind it to the variable
            interp.env.put(name, interp.evaluateSExp(val));
        }

        // evaluate the sexps that constitute this function
        Object result = interp.interpret(_progn);

        // copy the values of the shadowed variables back into the environment
        for (Map.Entry<String,Object> entry : shadowed.entrySet()) {
            Object oval = entry.getValue();
            if (oval == NO_VAL) interp.env.remove(entry.getKey());
            else interp.env.put(entry.getKey(), oval);
        }

        return result;
    }

    public int numArguments ()
    {
        return _args.size();
    }

    public String name ()
    {
        return _name;
    }

    //
    // UserFunction protected data members

    protected String _name;
    protected List _args;
    protected Progn _progn;

    protected static final Object NO_VAL = new Object();
}
