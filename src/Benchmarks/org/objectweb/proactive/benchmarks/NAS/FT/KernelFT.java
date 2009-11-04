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
package org.objectweb.proactive.benchmarks.NAS.FT;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.Kernel;
import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;
import org.objectweb.proactive.benchmarks.NAS.util.Reduce;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.timitspmd.util.BenchmarkStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.EventStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.HierarchicalTimerStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.TimItManager;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


/**
 * Kernel FT
 * 
 * A 3-D partial differential equation solution using FFTs. This kernel performs the essence of many
 * "spectral" codes. It is a rigorous test of long-distance communication performance.
 */
public class KernelFT extends Kernel {

    private FTProblemClass problemClass;
    private WorkerFT workers;

    public KernelFT() {
    }

    public KernelFT(NASProblemClass pclass, GCMApplication gcma) {
        this.problemClass = (FTProblemClass) pclass;
        this.gcma = gcma;
    }

    public void runKernel() throws ProActiveException {

        // Check if number of procs is a power of two.
        if (this.problemClass.NUM_PROCS != 1 &&
            ((this.problemClass.NUM_PROCS & (this.problemClass.NUM_PROCS - 1)) != 0)) {
            System.err.println("Error: nbprocs is " + this.problemClass.NUM_PROCS +
                " which is not a power of two");
            System.exit(1);
        }

        Node[] nodes;
        Reduce reduce;

        Object[] paramWorkers = new Object[] { problemClass };
        Object[] paramReduce = new Object[] {};
        Object[][] paramsWokers = new Object[problemClass.NUM_PROCS][];
        for (int i = 0; i < problemClass.NUM_PROCS; i++) {
            paramsWokers[i] = paramWorkers;
        }

        try {
            nodes = super.getNodes(this.problemClass.NUM_PROCS).toArray(new Node[] {});

            workers = (WorkerFT) PASPMD.newSPMDGroup(WorkerFT.class.getName(), paramsWokers, nodes);

            Node nodeWorker1 = null;

            try {
                nodeWorker1 = NodeFactory.getNode(PAActiveObject.getActiveObjectNodeUrl(PAGroup.get(workers,
                        PAGroup.size(workers) == 1 ? 0 : 1)));
            } catch (NodeException e) {
                e.printStackTrace();
            }

            reduce = PAActiveObject.newActive(Reduce.class, paramReduce, nodeWorker1);
            reduce.init(workers);

            TimItManager tManager = TimItManager.getInstance();
            tManager.setTimedObjects(workers);

            workers.start(reduce);

            BenchmarkStatistics bstats = tManager.getBenchmarkStatistics();
            HierarchicalTimerStatistics tstats = bstats.getTimerStatistics();
            EventStatistics estats = bstats.getEventsStatistics();

            Kernel.printEnd(this.problemClass, tstats.getMax(0, 0, 0), Double.valueOf(estats.getEventValue(
                    "mflops").toString()), (bstats.getInformation().indexOf("UNSUCCESSFUL") == -1));
            System.out.println(bstats);
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void killKernel() {
        workers.terminate();
    }

    public static void printStarted(String kernel, char className, long[] size, int nbIteration,
            int nbProcess, int np1, int np2, int layout_type) {
        Kernel.printStarted(kernel, className, size, nbIteration, nbProcess);
        System.out.println(" Processor array: " + np1 + " x " + np2);
        String layout;
        switch (layout_type) {
            case FTClasses.LAYOUT_0D:
                layout = "0D";
                break;
            case FTClasses.LAYOUT_1D:
                layout = "1D";
                break;
            case FTClasses.LAYOUT_2D:
                layout = "2D";
                break;
            default:
                layout = "Unknow Layout";
        }
        System.out.println(" Layout type: " + layout);
    }
}
