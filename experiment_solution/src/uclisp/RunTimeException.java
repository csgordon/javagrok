//
// RunTimeException -

package uclisp;
import org.javagrok.analysis.Retained;
import org.javagrok.analysis.UniqueReturn;

public class RunTimeException extends Exception
{
    //
    // RunTimeException public data members

    Object sexp;

    //
    // RunTimeException public constructor

    @UniqueReturn
    RunTimeException (String msg, @Retained Object sexp)
    {
        super(msg);
        this.sexp = sexp;
    }
}
