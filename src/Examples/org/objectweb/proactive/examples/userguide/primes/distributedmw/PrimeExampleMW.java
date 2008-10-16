/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
//@snippet-start primes_distributedmw_example
package org.objectweb.proactive.examples.userguide.primes.distributedmw;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;


/**
 * 
 * Some primes : 3093215881333057l, 4398042316799l, 63018038201, 2147483647
 * 
 * @author The ProActive Team
 * 
 */
public class PrimeExampleMW {
    /**
     * Default interval size
     */
    public static final int INTERVAL_SIZE = 100;

    public static void main(String[] args) {
        // The default value for the candidate to test (is prime)
        long candidate = 2147483647l;
        // Parse the number from args if there is some
        if (args.length > 1) {
            try {
                candidate = Long.parseLong(args[1]);
            } catch (NumberFormatException numberException) {
                System.err.println("Usage: PrimeExampleMW <candidate>");
                System.err.println(numberException.getMessage());
            }
        }
        try {
            //@snippet-start mw_primes_master_creation
            // Create the Master
            ProActiveMaster<FindPrimeTask, Boolean> master = new ProActiveMaster<FindPrimeTask, Boolean>();
            //@snippet-end mw_primes_master_creation
            //@snippet-start mw_primes_resources
            // Deploy resources
            master.addResources(new URL("file://" + args[0]));
            //@snippet-end mw_primes_resources
            // Create and submit the tasks
            master.solve(createTasks(candidate));

            //TODO 3. Wait all results from master */
            // Collect results            
            List<Boolean> results = master.waitAllResults();

            // Test the primality
            boolean isPrime = true;
            for (Boolean result : results) {
                isPrime = isPrime && result;
            }
            // Display the result
            System.out.println("\n" + candidate + (isPrime ? " is prime." : " is not prime.") + "\n");
            // Terminate the master and free all resources
            master.terminate(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /**
     * Creates the prime computation tasks to be solved
     * 
     * @return A list of prime computation tasks
     */
    public static List<FindPrimeTask> createTasks(long number) {
        List<FindPrimeTask> tasks = new ArrayList<FindPrimeTask>();

        // We don't need to check numbers greater than the square-root of the
        // candidate in this algorithm
        long squareRootOfCandidate = (long) Math.ceil(Math.sqrt(number));

        // Begin from 2 the first known prime number
        long begin = 2;

        // The number of intervals       
        long nbOfIntervals = (long) Math.ceil(squareRootOfCandidate / INTERVAL_SIZE);

        // Until the end of the first interval
        long end = INTERVAL_SIZE;

        for (int i = 0; i <= nbOfIntervals; i++) {

            //TODO 4. Create a new task for the current interval and 
            // add it to the list of tasks 
            // Adds the task for the current interval to the list of tasks
            tasks.add(new FindPrimeTask(number, begin, end));

            // Update the begin and the end of the interval
            begin = end + 1;
            end += INTERVAL_SIZE;
        }

        return tasks;
    }
}
//@snippet-end primes_distributedmw_example