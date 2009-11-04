/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.benchmarks.NAS.IS;

import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.Kernel;
import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.timitspmd.util.BenchmarkStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.EventStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.HierarchicalTimerStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.TimItManager;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


/**
 * Kernel IS
 *
 * A large integer sort. This kernel performs a sorting operation that is important in
 * "particle method" codes. It tests both integer computation speed and communication performance.
 */
public class KernelIS extends Kernel {

    private ISProblemClass problemClass;

    /** The reference for the typed group of Workers */
    private WorkerIS workers;

    public KernelIS() {
    }

    public KernelIS(NASProblemClass pclass, GCMApplication gcma) {
        this.problemClass = (ISProblemClass) pclass;
        this.gcma = gcma;
    }

    public void runKernel() throws ProActiveException {

        List<Node> nodes;
        AllBucketSize allBucketSize = null;

        try {
            nodes = super.getNodes(this.problemClass.NUM_PROCS);
            if (nodes == null) {
                throw new ProActiveException("No nodes found");
            }

            /* Creating AO for allBucketSize arrays management */
            int arraySize = this.problemClass.NUM_BUCKETS + this.problemClass.TEST_ARRAY_SIZE;

            Object[] param = new Object[] { this.problemClass };

            Object[][] params = new Object[this.problemClass.NUM_PROCS][];
            for (int i = 0; i < this.problemClass.NUM_PROCS; i++) {
                params[i] = param;
            }

            /* Creating group */
            workers = (WorkerIS) PASPMD.newSPMDGroup(WorkerIS.class.getName(), params, nodes
                    .toArray(new Node[] {}));

            WorkerIS[] workersArray = PAGroup.getGroup(workers).toArray(new WorkerIS[0]);

            // Get the chosen worker from the array
            WorkerIS chosenOne = (workersArray.length <= 1 ? workersArray[0] : workersArray[1]);

            // Get the node from the URL
            Node chosenOneNode = NodeFactory.getNode(PAActiveObject.getActiveObjectNodeUrl(chosenOne));

            allBucketSize = PAActiveObject.newActive(AllBucketSize.class, new Object[] { workers,
                    new Integer(this.problemClass.NUM_PROCS), Integer.valueOf(arraySize) }, chosenOneNode);

            workers.setAllBucketSize(allBucketSize);

            TimItManager tManager = TimItManager.getInstance();
            tManager.setTimedObjects(workers);

            workers.start();

            BenchmarkStatistics bstats = tManager.getBenchmarkStatistics();
            HierarchicalTimerStatistics tstats = bstats.getTimerStatistics();
            EventStatistics estats = bstats.getEventsStatistics();

            Kernel.printEnd(this.problemClass, tstats.getMax(0, 0, 0), Double.valueOf(estats.getEventValue(
                    "mflops").toString()), (bstats.getInformation().indexOf("UNSUCCESSFUL") == -1));

            System.out.println(tManager.getBenchmarkStatistics());
        } catch (ProActiveException e) {
            e.printStackTrace();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method will be called by the kill method of the Benchmark class to terminate all
     * workers.
     */
    public void killKernel() {
        workers.terminate();

        Group<WorkerIS> g = PAGroup.getGroup(workers);
        ProxyForGroup<WorkerIS> p = (ProxyForGroup<WorkerIS>) g;
        p.finalize();
    }
}