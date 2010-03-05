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

    public boolean isEmpty ()
    {
        return this == nil;
    }

    public int size ()
    {
        return isEmpty() ? 0 : (cdr.size() + 1);
    }

    public List reverse ()
    {
        List rev = List.nil, iter = this;
        while (!iter.isEmpty()) {
            rev = rev.cons(iter.car);
            iter = iter.cdr;
        }
        return rev;
    }

    protected List (Object car, List cdr)
    {
        this.car = car;
        this.cdr = cdr;
    }
}
