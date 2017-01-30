/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.util;

import java.security.SecureRandom;


/**
 * Provides an easy to get a random values for a SecureRandom PRNG
 *
 * A single PRNG is shared for the whole ProActive Runtime.
 *
 * @see SecureRandom
 */
public final class ProActiveRandom {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // public for testing purposes, the content should never be altered
    public static final char[] SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    /** Returns the next pseudorandom, uniformly distributed boolean value from this random number generator's sequence. */
    public static boolean nextBoolean() {
        return SECURE_RANDOM.nextBoolean();
    }

    /** Generates random bytes and places them into a user-supplied byte array. */
    public static void nextBytes(byte[] bytes) {
        SECURE_RANDOM.nextBytes(bytes);
    }

    /** Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence. */
    public static double nextDouble() {
        return SECURE_RANDOM.nextDouble();
    }

    /**  Returns the next pseudorandom, uniformly distributed float  value between 0.0 and 1.0 from this random number generator's sequence. */
    public static float nextFloat() {
        return SECURE_RANDOM.nextFloat();
    }

    /** Returns the next pseudorandom, uniformly distributed int  value from this random number generator's sequence.*/
    public static int nextInt() {
        return SECURE_RANDOM.nextInt();
    }

    /** Returns the next pseudorandom, uniformly distributed positive int  value from this random number generator's sequence.*/
    public static int nextPosInt() {
        return SECURE_RANDOM.nextInt(Integer.MAX_VALUE);
    }

    /** Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the specified value (exclusive), drawn from this random number generator's sequence. */
    public static int nextInt(int n) {
        return SECURE_RANDOM.nextInt(n);
    }

    /** Returns the next pseudorandom, uniformly distributed long  value from this random number generator's sequence. */
    public static long nextLong() {
        return SECURE_RANDOM.nextLong();
    }

    public static long nextPosLong() {
        return Math.abs(nextLong());
    }

    /**
     * Returns a random string of fixed length
     * 
     * The string will only characters from {@link ProActiveRandom#SYMBOLS}
     * (upper case alphanumeric ASCII SYMBOLS).
     * 
     * @param size the length of the random string 
     * @return A random string
     */
    public static String nextString(int size) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            sb.append(SYMBOLS[nextInt(SYMBOLS.length)]);
        }

        return sb.toString();
    }

}
