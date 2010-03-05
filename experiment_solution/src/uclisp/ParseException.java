//
// ParseException - an exception to indicate problems with parsing

package uclisp;
import org.javagrok.analysis.NonUniqueReturn;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.UniqueReturn;

public class ParseException extends Exception
{
    //
    // ParseException public constructor

    @UniqueReturn
    public ParseException (String msg, int lineno, String token)
    {
        super(msg);
        _lineno = lineno;
        _token = token;
    }

    //
    // ParseException public member functions

    @LentThis
    public int lineNumber ()
    {
        return _lineno;
    }

    @LentThis
    @NonUniqueReturn
    public String token ()
    {
        return _token;
    }

    //
    // ParseException protected data members

    int _lineno;

    String _token;
}
