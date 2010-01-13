/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.examples.masterworker;

import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.api.PALifeCycle;


public class PIExample {
    public static final long NUMBER_OF_EXPERIENCES = 1000000;
    public static final int NUMBER_OF_TASKS = 30;

    public static void main(String[] args) throws TaskException, ProActiveException {

        findOS();
        //@snippet-start masterworker_montecarlopi_master_creation
        // creation of the master
        ProActiveMaster<ComputePIMonteCarlo, Long> master = new ProActiveMaster<ComputePIMonteCarlo, Long>();

        // adding resources
        master.addResources(PIExample.class.getResource("MWApplication.xml"));
        //@snippet-end masterworker_montecarlopi_master_creation
        //@snippet-start masterworker_montecarlopi_tasks_submit
        // defining tasks
        Vector<ComputePIMonteCarlo> tasks = new Vector<ComputePIMonteCarlo>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            tasks.add(new ComputePIMonteCarlo());
        }

        // adding tasks to the queue
        master.solve(tasks);
        //@snippet-end masterworker_montecarlopi_tasks_submit
        //@snippet-start masterworker_montecarlopi_results
        // waiting for results
        List<Long> successesList = master.waitAllResults();

        // computing PI using the results
        long sumSuccesses = 0;

        for (long successes : successesList) {
            sumSuccesses += successes;
        }

        double pi = (4 * sumSuccesses) / ((double) NUMBER_OF_EXPERIENCES * NUMBER_OF_TASKS);

        System.out.println("Computed PI by Monte-Carlo method : " + pi);
        //@snippet-end masterworker_montecarlopi_results
        //@snippet-start masterworker_montecarlopi_terminate
        master.terminate(true);
        //@snippet-end masterworker_montecarlopi_terminate

        PALifeCycle.exitSuccess();
    }

    public static void findOS() {

    }

    //@snippet-start masterworker_montecarlopi
    /**
     * Task which creates randomly a set of points belonging to the [0, 1[x[0, 1[ interval<br>
     * and tests how many points are inside the uniter circle.
     * @author The ProActive Team
     *
     */
    public static class ComputePIMonteCarlo implements Task<Long> {

        /**
         * 
         */
        private static final long serialVersionUID = 420L;

        /**
         *
         */
        public ComputePIMonteCarlo() {
        }

        public Long run(WorkerMemory memory) throws Exception {
            long remaining = NUMBER_OF_EXPERIENCES;
            long successes = 0;
            while (remaining > 0) {
                remaining--;
                if (experience()) {
                    successes++;
                }
            }
            return successes;
        }

        public boolean experience() {
            double x = Math.random();
            double y = Math.random();
            return Math.hypot(x, y) < 1;
        }
    }
    //@snippet-end masterworker_montecarlopi
}
