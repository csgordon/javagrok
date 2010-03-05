//
// Scanner -

package uclisp;

import java.io.Reader;
import java.io.StreamTokenizer;

public class Scanner extends StreamTokenizer
{
    //
    // Scanner public constructor

    public Scanner (Reader in)
    {
        super(in);

        // initialize the stream tokenizer for lisp
        commentChar(';');
        parseNumbers();
        quoteChar('"');
        ordinaryChar('\'');
        wordChars('_', '_');
    }
}
