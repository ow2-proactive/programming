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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.benchmarks.NAS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.objectweb.proactive.api.PAVersion;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * A new NAS kernel must extend this class to be executable
 * 
 */
public abstract class Kernel implements Serializable {
    protected GCMApplication gcma;

    public abstract void runKernel() throws ProActiveException;

    public abstract void killKernel();

    public static void printStarted(String kernel, char className, long[] size, int nbIteration, int nbProcess) {
        System.out.print("\n\n NAS Parallel Benchmarks ProActive -- " + kernel + " Benchmark\n\n");
        System.out.println(" Class: " + className);
        System.out.print(" Size:  " + size[0]);
        for (int i = 1; i < size.length; i++) {
            System.out.print(" x " + size[i]);
        }
        System.out.println();

        System.out.println(" Iterations:   " + nbIteration);
        System.out.println(" Number of processes:     " + nbProcess);
    }

    public static void printEnd(NASProblemClass clss, double totalTime, double mops,
            boolean passed_verification) {
        String verif;
        String javaVersion = System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.name") +
            " " + System.getProperty("java.vm.version") + " - Version " + System.getProperty("java.version");
        String proactiveVersion = "ProActive " + PAVersion.getProActiveVersion();

        verif = passed_verification ? "SUCCESSFUL" : "UNSUCCESSFUL";

        System.out.println("\n\n " + clss.KERNEL_NAME + " Benchmark Completed");
        System.out.println(" Class            =  " + clss.PROBLEM_CLASS_NAME);
        System.out.println(" Size             =  " + clss.SIZE);
        System.out.println(" Iterations       =  " + clss.ITERATIONS);
        System.out.println(" Time in seconds  =  " + totalTime);
        System.out.println(" Total processes  =  " + clss.NUM_PROCS);
        System.out.println(" Mop/s total      =  " + mops);
        System.out.println(" Mop/s/process    =  " + (mops / clss.NUM_PROCS));
        System.out.println(" Operation type   =  " + clss.OPERATION_TYPE);
        System.out.println(" Verification     =  " + verif);
        System.out.println(" NPB Version      =  " + clss.VERSION);
        System.out.println(" Java RE          =  " + javaVersion);
        System.out.println(" Middleware       =  " + proactiveVersion);
    }

    public List<Node> getNodes(int count) {
        GCMVirtualNode vn = gcma.getVirtualNode("Workers");
        if (!vn.isGreedy() && vn.getNbRequiredNodes() < count) {
            throw new IllegalStateException("Not enough node available: " + vn.getNbRequiredNodes() +
                " in GCMA but nproc is " + count);
        }

        ArrayList<Node> nodes = new ArrayList<Node>();

        gcma.startDeployment();
        // Wait for all nodes to be able to use the best combination of nodes
        gcma.waitReady();

        // Group nodes by TopologyID
        Map<Long, List<Node>> nodesByTID = new HashMap<Long, List<Node>>();
        for (Node node : vn.getCurrentNodes()) {
            long tid = node.getVMInformation().getTopologyId();
            List<Node> nByTid = nodesByTID.get(tid);
            if (nByTid == null) {
                nByTid = new ArrayList<Node>();
                nodesByTID.put(tid, nByTid);
            }
            nByTid.add(node);
        }

        // Sort group by # of nodes
        SortedMap<Long, List<Node>> nodesBySize = new TreeMap<Long, List<Node>>();
        for (Long tid : nodesByTID.keySet()) {
            List<Node> tempNodes = nodesByTID.get(tid);
            nodesBySize.put(new Long(tempNodes.size()), tempNodes);
        }

        // Return count nodes by using the largest list first
        while (nodes.size() < count) {
            List<Node> currentList = null;
            do {
                currentList = nodesBySize.get(nodesBySize.lastKey());
                if (currentList.isEmpty()) {
                    nodesBySize.remove(nodesBySize.lastKey());
                }
            } while (currentList == null);

            nodes.add(currentList.remove(0));
        }

        return nodes;
    }
}