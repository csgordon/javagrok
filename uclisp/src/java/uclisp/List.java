//
// List - one data structure to rule them all

package uclisp;

public class List
{
    /** The empty list. */
    public static final List nil = new List(null, null);

    /** This list's head. */
    public final Object car;

    /** This list's tail. May be {@link #null}. */
    public final List cdr;

    public List cons (Object head)
    {
        return new List(head, this);
    }

    public Object elementAt (int index)
    {
        if (index < 0 || this == nil) {
            throw new IndexOutOfBoundsException();
        }
        return  (index == 0) ? car : cdr.elementAt(index-1);
    }

    public boolean isNil ()
    {
        return this == nil;
    }

    public int size ()
    {
        return isNil() ? 0 : (cdr.size() + 1);
    }

    public List reverse ()
    {
        List rev = List.nil;
        List iter = this;
        while (!iter.isNil()) {
            rev = rev.cons(iter.car);
            iter = iter.cdr;
        }
        return rev;
    }

    @Override public String toString ()
    {
        StringBuilder buf = new StringBuilder("(");
        for (List l = this; !l.isNil(); l = l.cdr) {
            if (buf.length() > 1) {
                buf.append(" ");
            }
            buf.append(l.car);
        }
        return buf.append(")").toString();
    }

    protected List (Object car, List cdr)
    {
        this.car = car;
        this.cdr = cdr;
    }
}
