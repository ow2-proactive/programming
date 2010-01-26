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
//@snippet-start primes_distributed_worker
//@snippet-start primes_distributed_worker_skeleton
//@tutorial-start
package org.objectweb.proactive.examples.userguide.primes.distributed;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.examples.userguide.cmagent.simple.CMAgent;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * @author The ProActive Team
 */
@ActiveObject
public class CMAgentPrimeWorker extends CMAgent {

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
    public BooleanWrapper isPrime(final long candidate, final long begin, final long end) {
        try {
            //Used for slowing down the application for in order 
            //to let one stop it for checking fault tolerance behavior
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //TODO 4. Return a reifiable wrapper for the Boolean type
        //    for asynchronous calls. 
        //@snippet-break primes_distributed_worker_skeleton
        //@tutorial-break
        for (long divider = begin; divider < end; divider++) {
            if ((candidate % divider) == 0) {
                return new BooleanWrapper(false);
            }
        }
        return new BooleanWrapper(true);
        //@tutorial-resume
        //@snippet-resume primes_distributed_worker_skeleton
    }

}
//@tutorial-end
//@snippet-end primes_distributed_worker_skeleton
//@snippet-end primes_distributed_worker
