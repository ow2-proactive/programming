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
//@snippet-start primes_distributed_manager
//@snippet-start primes_distributed_manager_skeleton
package org.objectweb.proactive.examples.userguide.primes.distributed;

import java.util.Vector;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * @author The ProActive Team
 */
@ActiveObject
public class CMAgentPrimeManager {
    /**
     * A vector of references on workers
     */
    private Vector<CMAgentPrimeWorker> workers = new Vector<CMAgentPrimeWorker>();
    /**
     * Default interval size
     */
    public static final int INTERVAL_SIZE = 100;

    /**
     * Empty no-arg constructor needed by ProActive
     */
    public CMAgentPrimeManager() {
    }

    /**
     * Tests a primality of a specified number. Synchronous !
     * 
     * @param number
     *            The number to test
     * @return <code>true</code> if is prime; <code>false</code> otherwise
     */
    public boolean isPrime(long number) {
        // We don't need to check numbers greater than the square-root of the
        // candidate in this algorithm
        long squareRootOfCandidate = (long) Math.ceil(Math.sqrt(number));

        // Begin from 2 the first known prime number
        long begin = 2;

        // The number of intervals       
        long nbOfIntervals = (long) Math.ceil(squareRootOfCandidate / INTERVAL_SIZE);

        // Until the end of the first interval
        long end = INTERVAL_SIZE;

        // The vector of futures
        final Vector<BooleanWrapper> answers = new Vector<BooleanWrapper>();

        // Non blocking (asynchronous method call) 
        for (int i = 0; i <= nbOfIntervals; i++) {

            // Use round robin selection of worker
            int workerIndex = i % workers.size();
            CMAgentPrimeWorker worker = workers.get(workerIndex);

            //TODO 1. Send asynchronous method call to the worker 
            //@snippet-break primes_distributed_manager_skeleton
            // Send asynchronous method call to the worker
            //@tutorial-break
            BooleanWrapper res = worker.isPrime(number, begin, end);
            //@tutorial-resume
            //@snippet-resume primes_distributed_manager_skeleton

            //TODO 2. Add the future result to the vector of answers 
            //@snippet-break primes_distributed_manager_skeleton
            // Adds the future to the vector
            //@tutorial-break
            answers.add(res);
            //@tutorial-resume
            //@snippet-resume primes_distributed_manager_skeleton

            // Update the begin and the end of the interval
            begin = end + 1;
            end += INTERVAL_SIZE;
        }
        // Once all requests was sent
        boolean prime = true;
        int intervalNumber = 0;
        // Loop until a worker returns false or vector is empty (all results have been checked)
        while (!answers.isEmpty() && prime) {

            // TODO 3. Block until a new response is available 
            // by using a static method from org.objectweb.proactive.api.PAFuture
            //@snippet-break primes_distributed_manager_skeleton
            //@tutorial-break
            // Will block until a new response is available
            intervalNumber = PAFuture.waitForAny(answers);
            //@tutorial-resume
            //@snippet-resume primes_distributed_manager_skeleton

            // Check the answer
            prime = answers.get(intervalNumber).booleanValue();

            // Remove the actualized future			
            answers.remove(intervalNumber);

        }
        return prime;
    }

    /**
     * Adds a worker to the local vector
     * 
     * @param worker
     *            The worker to add to the vector
     */
    public void addWorker(CMAgentPrimeWorker worker) {
        this.workers.add(worker);
    }
}
// @snippet-end primes_distributed_manager
//@snippet-end primes_distributed_manager_skeleton
//@tutorial-end
