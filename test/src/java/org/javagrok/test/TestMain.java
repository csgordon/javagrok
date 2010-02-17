//
// $Id$

package org.javagrok.test;

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

        /** Increments the boxed integer by the specified amount. */
        public void increment (int amount) {
            _currentValue += amount;
        }

        private int _currentValue;
    }

    public static void main (String[] args)
    {
        IntBox box = new IntBox(5);
        box.increment(3);
        System.out.println("Value " + box.current());
    }

    public TestMain () {
        _box = new IntBox(0);
    }

    public void plusplus () {
        _box.increment(1);
    }

    public void maybeFail (boolean shouldFail) {
        if (shouldFail) {
            throw new RuntimeException("Epic FAIL!");
        }
    }

    private IntBox _box;
}
