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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
//@snippet-start primes_distributed_main
package org.objectweb.proactive.examples.userguide.primes.distributed;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.simple.CMAgent;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * This class illustrates a distributed version of the sequential algorithm for
 * primality test based on the {@link CMAgent}.
 * <p>
 * Some primes : 3093215881333057, 4398042316799, 63018038201, 2147483647 
 * 
 * @author The ProActive Team
 * 
 */
public class Main {

    private static GCMApplication pad;

    private static Collection<GCMVirtualNode> deploy(String descriptor) {
        try {
            pad = PAGCMDeployment.loadApplicationDescriptor(new File(descriptor));
            //active all Virtual Nodes
            pad.startDeployment();
            pad.waitReady();

            return pad.getVirtualNodes().values();
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ProActiveException proExcep) {
            System.err.println(proExcep.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        // The default value for the candidate to test (is prime)
        long candidate = 2147483647l;
        // Parse the number from args if there is some
        if (args.length > 1) {
            try {
                candidate = Long.parseLong(args[1]);
            } catch (NumberFormatException numberException) {
                System.err.println("Usage: Main <candidate>");
                System.err.println(numberException.getMessage());
            }
        }

        try {
            Collection<GCMVirtualNode> vNodes = deploy(args[0]);
            GCMVirtualNode vNode = vNodes.iterator().next();

            // create the active object on the first node on
            // the first virtual node available
            // start the master
            CMAgentPrimeManager manager = (CMAgentPrimeManager) PAActiveObject.newActive(
                    CMAgentPrimeManager.class.getName(), new Object[] {}, vNode.getANode());

            //TODO 5:  iterate through all nodes, deploy
            // a worker per node and add it to the manager

            Iterator<Node> nodesIt = vNode.getCurrentNodes().iterator();
            while (nodesIt.hasNext()) {
                Node node = nodesIt.next();
                CMAgentPrimeWorker worker = (CMAgentPrimeWorker) PAActiveObject.newActive(
                        CMAgentPrimeWorker.class.getName(), new Object[] {}, node);
                manager.addWorker(worker);
            }

            // Check the primality (Send a synchronous method call to the manager)
            boolean isPrime = manager.isPrime(candidate);
            // Display the result
            System.out.println("\n" + candidate + (isPrime ? " is prime." : " is not prime.") + "\n");
            // Free all resources
            pad.kill();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

}
// @snippet-end primes_distributed_main
