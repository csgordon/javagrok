//
// Scanner -

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.UniqueReturn;
import java.io.InputStream;
import java.io.IOException;
import java.io.StreamTokenizer;

public class Scanner extends StreamTokenizer
{
    //
    // Scanner public constructor

    @UniqueReturn
    public Scanner (@Retained InputStream is)
    {
        super(is);

        // initialize the stream tokenizer for lisp
        commentChar(';');
        parseNumbers();
        quoteChar('"');
        ordinaryChar('\'');
        wordChars('_', '_');
    }
}
