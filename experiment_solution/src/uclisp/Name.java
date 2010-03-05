//
// Name - A class used to represent a name

package uclisp;
import org.javagrok.analysis.NonUniqueReturn;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.UniqueReturn;

public class Name
{
    //
    // Name public constructor

    @UniqueReturn
    public Name (String value)
    {
        _string = value;
    }

    //
    // Name public member functions

    @LentThis
    @NonUniqueReturn
    public String toString ()
    {
        return _string;
    }

    //
    // Name protected data members

    String _string;
}
