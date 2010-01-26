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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.userguide.distributedprimes;

import java.io.Serializable;
import java.util.Vector;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class PrimeWorker implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    private Vector<Long> primes = new Vector<Long>();

    public PrimeWorker() {
    }//empty no-arg constructor needed by ProActive

    //check for primes
    public BooleanWrapper isPrime(long number) {
        if (primes.isEmpty()) //if no number has been received yet
            return new BooleanWrapper(true);
        else {
            int i = 0;
            int size = primes.size(); //store the size of the vector so we don't call the size() method repeatedly
            long value = number; //store the value so we don't call the longValue() repeatedly
            while ((i < size) && //has not reached the end of the Vector
                (value % primes.get(i).longValue() != 0))
                //does not divide
                i++;
            //if it goes through all the Vector then it is prime
            if (i == size)
                return new BooleanWrapper(true);
            else
                return new BooleanWrapper(false);
        }
    }

    //add a prime to the Vector 
    public void addPrime(Long number) {
        primes.add(number);
    }
}
