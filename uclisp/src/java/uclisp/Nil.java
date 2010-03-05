//
// Nil - a null value

package uclisp;

public class Nil
{
    public static final Nil nil = new Nil();

    @Override public String toString ()
    {
        return "nil";
    }
}
