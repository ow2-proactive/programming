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
package org.objectweb.proactive.benchmarks.NAS.CG;

import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.Kernel;
import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;
import org.objectweb.proactive.benchmarks.NAS.util.NpbMath;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.timitspmd.util.BenchmarkStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.EventStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.HierarchicalTimerStatistics;
import org.objectweb.proactive.extensions.timitspmd.util.TimItManager;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


/**
 * NAS PARALLEL BENCHMARKS
 *
 * Kernel CG
 *
 * A conjugate gradient method is used to compute an approximation
 * to the smallest eigenvalue of a large, sparse, symmetric positive
 * definite matrix. This kernel is typical of unstructured grid
 * computations in that it tests irregular long distance communication,
 * employing unstructured matrix vector multiplication.
 */
public class KernelCG extends Kernel {

    public static boolean ROUND_ROBIN_MAPPING_MODE = true;

    private CGProblemClass problemClass;

    /** Initial values calculated locally */
    private int npcols;
    private int nprows;
    private int nzz;

    /** The reference for the typed group of Workers */
    private WorkerCG workers;

    public KernelCG() {
    }

    public KernelCG(NASProblemClass pclass, GCMApplication gcma) {
        this.problemClass = (CGProblemClass) pclass;
        this.gcma = gcma;
    }

    public void runKernel() throws ProActiveException {
        // String buff to have a print order
        String printBuffer = "";
        // Some int values
        int i;
        // The nodes to use
        List<Node> nodes = null;
        // Temp node array
        //        Node[] tempArray = null;
        // The array of parameters for each worker
        Object[] param;
        // The array of parameters for all workers
        Object[][] params;

        try {
            // Check if number of procs is a power of two.
            if (this.problemClass.NUM_PROCS != 1 &&
                ((this.problemClass.NUM_PROCS & (this.problemClass.NUM_PROCS - 1)) != 0)) {
                System.err.println("Error: nbprocs is " + this.problemClass.NUM_PROCS +
                    " which is not a power of two");
                System.exit(1);
            }

            // Calculate the
            this.npcols = this.nprows = NpbMath.ilog2(this.problemClass.NUM_PROCS) / 2;

            //???
            if ((this.npcols + this.nprows) != NpbMath.ilog2(this.problemClass.NUM_PROCS)) {
                this.npcols += 1;
            }
            this.npcols = (int) NpbMath.ipow2(this.npcols);
            this.nprows = (int) NpbMath.ipow2(this.nprows);

            // Check npcols parity
            if (this.npcols != 1 && ((this.npcols & (this.npcols - 1)) != 0)) {
                System.err.println("Error: num_proc_cols is " + this.npcols + " which is not a power of two");
                System.exit(1);
            }

            // Check nprows parity
            if (this.nprows != 1 && ((this.nprows & (this.nprows - 1)) != 0)) {
                System.err.println("Error: num_proc_rows is " + this.nprows + " which is not a power of two");
                System.exit(1);
            }

            nodes = super.getNodes(this.problemClass.NUM_PROCS);
            if (nodes == null) {
                throw new ProActiveException("No nodes found");
            }

            printBuffer += "" + nodes.size() + " node" + (nodes.size() == 1 ? "" : "s") + " found\n";
            System.out.println(printBuffer);

            // Pre-calculate the nzz value
            this.nzz = ((this.problemClass.na * (this.problemClass.nonzer + 1) * (this.problemClass.nonzer + 1)) / this.problemClass.NUM_PROCS) +
                ((this.problemClass.na * (this.problemClass.nonzer + 2 + (this.problemClass.NUM_PROCS / 256))) / this.npcols);

            ////////////////////////////////////////////////////////////////////
            // Group creation begins here ...
            ////////////////////////////////////////////////////////////////////

            // Fill the constructor arguments
            param = new Object[] { problemClass, Integer.valueOf(this.npcols), Integer.valueOf(this.nprows),
                    Integer.valueOf(this.nzz) };
            params = new Object[this.problemClass.NUM_PROCS][];
            for (i = 0; i < problemClass.NUM_PROCS; i++) {
                params[i] = param;
            }

            // Create the workers group
            workers = (WorkerCG) PASPMD.newSPMDGroup(WorkerCG.class.getName(), params, nodes
                    .toArray(new Node[] {}));

            TimItManager tManager = TimItManager.getInstance();
            tManager.setTimedObjects(workers);

            workers.start();

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
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will be called by the kill method of the Benchmark class
     * to terminate all workers.
     */
    public void killKernel() {
        workers.terminate();

        Group<WorkerCG> g = PAGroup.getGroup(workers);
        ProxyForGroup<WorkerCG> p = (ProxyForGroup<WorkerCG>) g;
        p.finalize();
    }

    public static void printStarted(String kernel, char className, long[] size, int nbIteration,
            int nbProcess, int nonzer, int shift) {
        Kernel.printStarted(kernel, className, size, nbIteration, nbProcess);
        System.out.println(" Number of nonzeroes per rows: " + nonzer);
        System.out.println(" Eigenvalue shift: " + shift);
    }
}
