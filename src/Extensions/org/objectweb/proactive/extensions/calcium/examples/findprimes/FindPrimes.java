/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.calcium.examples.findprimes;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.Calcium;
import org.objectweb.proactive.extensions.calcium.Stream;
import org.objectweb.proactive.extensions.calcium.environment.Environment;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.futures.CalFuture;
import org.objectweb.proactive.extensions.calcium.skeletons.DaC;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;
import org.objectweb.proactive.extensions.calcium.statistics.Stats;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobal;


public class FindPrimes implements Serializable {
    // @snippet-start calcium_primes_1
    public Skeleton<Interval, Primes> root;
    // @snippet-break calcium_primes_1
    
    public static void main(String[] args) throws InterruptedException, PanicException {
        FindPrimes st = new FindPrimes();
        st.solve();
    }

    // @snippet-resume calcium_primes_1
    public FindPrimes() {
        root = new DaC<Interval, Primes>(new IntervalDivide(), new IntervalDivideCondition(),
            new SearchInterval(), new JoinPrimes());
    }
    // @snippet-end calcium_primes_1

    public void solve() throws InterruptedException, PanicException {
        // @snippet-start calcium_primes_15
        // @snippet-start calcium_primes_17
        String descriptor = FindPrimes.class.getResource("LocalDescriptor.xml").getPath();
        // @snippet-break calcium_primes_17
        // @snippet-break calcium_primes_15
        
        // @snippet-start calcium_primes_8
        // @snippet-start calcium_primes_14
        Environment environment = EnvironmentFactory.newMultiThreadedEnvironment(2);
        // @snippet-end calcium_primes_14
        // @snippet-break calcium_primes_8
        /*
        // @snippet-resume calcium_primes_15
        Environment environment = EnvironmentFactory.newProActiveEnvironment(descriptor);
        // @snippet-end calcium_primes_15
        */
        /*
        // @snippet-resume calcium_primes_17
        Environment environment = EnvironmentFactory.newProActiveEnviromentWithGCMDeployment(descriptor);
        // @snippet-end calcium_primes_17
        */
        //Environment environment = ProActiveSchedulerEnvironment.factory("localhost","chri", "chri");
        // @snippet-resume calcium_primes_8
        
        Calcium calcium = new Calcium(environment);
        
        Stream<Interval, Primes> stream = calcium.newStream(root);
        // @snippet-end calcium_primes_8
        
        // @snippet-start calcium_primes_9
        Vector<CalFuture<Primes>> futures = new Vector<CalFuture<Primes>>(3);
        futures.add(stream.input(new Interval(1, 6400, 300)));
        futures.add(stream.input(new Interval(1, 100, 20)));
        futures.add(stream.input(new Interval(1, 640, 64)));
        calcium.boot(); //begin the evaluation
        // @snippet-end calcium_primes_9
        
        try {
            // @snippet-start calcium_primes_10
            // @snippet-start calcium_resultStats
            for (CalFuture<Primes> future : futures) {
                Primes res = future.get();
                // @snippet-break calcium_resultStats
                for (Integer i : res.primes) {
                    System.out.print(i + " ");
                }
                // @snippet-break calcium_primes_10
                System.out.println();
                // @snippet-start calcium_primes_11
                // @snippet-resume calcium_resultStats
                Stats futureStats = future.getStats();
                System.out.println(futureStats);
                // @snippet-end calcium_primes_11
                // @snippet-resume calcium_primes_10
            }
            // @snippet-end calcium_resultStats
            // @snippet-end calcium_primes_10
        } catch (MuscleException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // @snippet-start calcium_primes_12
        StatsGlobal stats = calcium.getStatsGlobal();
        System.out.println(stats);
        // @snippet-end calcium_primes_12
        // @snippet-start calcium_primes_13
        calcium.shutdown();
        // @snippet-end calcium_primes_13
    }
}
