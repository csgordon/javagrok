//
// $Id$

package org.javagrok.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple test program on which to test analyses and JavaGrok.
 */
public class TestMain
{
    /** A little box, that holds an integer. */
    public static class IntBox {
        /** The initial value stored in the box. */
        public int initialValue;

        public IntBox (int value) {
            this.initialValue = value;
            _currentValue = value;
        }

        /** Returns the current value of our integer. */
        public int current () {
            return _currentValue;
        }
        
        public int five() {
            // five() does not lend the receiver --> @NotLentThis!
            lastIntBoxOnWhichFiveWasCalled = this;
            return 5;
        }

        /** Increments the boxed integer by the specified amount. */
        public void increment (int amount) {
            _currentValue += amount;
        }

        private int _currentValue;
    }
    
    private static IntBox lastIntBoxOnWhichFiveWasCalled = null;

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {
    }

    public static void main (String[] args) {
        IntBox box = new IntBox(5);
        box.increment(3);
        System.out.println("Value " + box.current());

        // taint noop!
        String nn = noop(null);

        String foo = "foo";
        int lfoo = length(noop(foo));
    }

    public static int length (String value) {
        return value.length();
    }

    public TestMain () {
        _box = new IntBox(0);
        list = new LinkedList<Object>();
    }

    public void plusplus () {
        _box.increment(1);
    }

    public void maybeFail (boolean shouldFail) {
        if (shouldFail) {
            throw new RuntimeException("Epic FAIL!");
        }
    }

    private static String noop (String value) {
        if (value == null) {
            return "";
        }
        int hc = value.hashCode();
        return value;
    }

    private IntBox _box;
    
    private LinkedList<Object> list;
    
    public void setSomeObject(Object so) {
        list.add(so);
    }
    
    public void setSomeObject2(Object so) {
        list.add(new Object());
    }

    public Object getSomeObject(int x) {
        if (x < 0) throw new IllegalArgumentException();
        return list.get(x % list.size());
    }
    
    public List<Object> getAll() {
        return list;
    }
    
    public List<Object> getAll2() {
        LinkedList<Object> linkedList = new LinkedList<Object>();
        linkedList.addAll(this.list);
        return linkedList;
    }
}
