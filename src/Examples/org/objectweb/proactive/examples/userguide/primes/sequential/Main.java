/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
//@snippet-start primes_sequential_main
package org.objectweb.proactive.examples.userguide.primes.sequential;

/**
 * This class illustrates a sequential algorithm for primality test.
 * <p>
 * Some primes : 4398042316799l, 63018038201, 2147483647
 * 
 * @author The ProActive Team
 * 
 */
public class Main {

    public static void main(String[] args) {
        // The default value for the candidate to test (is prime)
        long candidate = 3093215881333057l;
        // Parse the number from args if there is some
        if (args.length > 0) {
            try {
                candidate = Long.parseLong(args[0]);
            } catch (NumberFormatException numberException) {
                System.err.println(numberException.getMessage());
                System.err.println("Usage: Main <candidate>");
            }
        }
        // We don't need to check numbers greater than the square-root of the
        // candidate in this algorithm
        long squareRootOfCandidate = (long) Math.ceil(Math.sqrt(candidate));
        // Begin from 2 the first known prime number
        long begin = 2;
        // Until the end of the range
        long end = squareRootOfCandidate;
        // Check the primality
        boolean isPrime = Main.isPrime(candidate, begin, end);
        // Display the result
        System.out.println("\n" + candidate + (isPrime ? " is prime." : " is not prime.") + "\n");
    }

    /**
     * Tests a primality of a specified number in a specified range.
     * 
     * @param candidate
     *            the candidate number to check
     * @param begin
     *            starts check from this value
     * @param end
     *            checks until this value
     * @return <code>true</code> if is prime; <code>false</code> otherwise
     */
    public static Boolean isPrime(long candidate, long begin, long end) {
        for (long divider = begin; divider < end; divider++) {
            if ((candidate % divider) == 0) {
                return false;
            }
        }
        return true;
    }
}
//@snippet-end primes_sequential_main
//@tutorial-end
