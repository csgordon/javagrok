//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util;

import java.util.Random;

/**
 * Provides miscellaneous utility routines to simplify obtaining useful random number values and to
 * centralize seeding and proper care and feeding of the pseudo-random number generator.
 */
public class RandomUtil
{
    /** The random number generator used by the methods in this class. */
    public static final Random rand = new Random();

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between 0 (inclusive)
     * and the specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public static int getInt (int high)
    {
        return getInt(high, rand);
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between 0 (inclusive)
     * and the specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     * @param r the random number generator to use
     */
    public static int getInt (int high, Random r)
    {
        return r.nextInt(high);
    }

    /**
     * Returns a pseudorandom, uniformly distributed float value between 0.0 (inclusive) and the
     * specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public static float getFloat (float high)
    {
        return getFloat(high, rand);
    }

    /**
     * Returns a pseudorandom, uniformly distributed float value between 0.0 (inclusive) and the
     * specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     * @param r the random number generator to use
     */
    public static float getFloat (float high, Random r)
    {
        return r.nextFloat() * high;
    }

    /**
     * Returns a pseudorandom, uniformly distributed boolean.
     */
    public static boolean getBoolean ()
    {
        return getBoolean(rand);
    }

    /**
     * Returns a pseudorandom, uniformly distributed boolean.
     *
     * @param r the random number generator to use
     */
    public static boolean getBoolean (Random r)
    {
        return r.nextBoolean();
    }
}
