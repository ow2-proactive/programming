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
package org.objectweb.proactive.examples.userguide.cmagent.synch;

import java.io.File;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.simple.State;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Main {
    private static GCMVirtualNode deploy(String descriptor) {
        GCMApplication pad;
        GCMVirtualNode vn;
        try {
            //create object representation of the deployment file
            pad = PAGCMDeployment.loadApplicationDescriptor(new File(descriptor));
            //active all Virtual Nodes
            pad.startDeployment();
            pad.waitReady();
            //get the first Node available in the first Virtual Node 
            //specified in the descriptor file
            vn = pad.getVirtualNodes().values().iterator().next();
            return vn;
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ProActiveException proExcep) {
            System.err.println(proExcep.getMessage());
        }
        return null;
    }

    //@snippet-start synch_cma_main
    public static void main(String args[]) {
        GCMVirtualNode vn = deploy(args[0]);
        Vector<CMAgentChained> agents = new Vector<CMAgentChained>();
        //create the active objects
        try {
            //create a collection of active objects
            for (Node node : vn.getCurrentNodes()) {
                CMAgentChained ao = (CMAgentChained) PAActiveObject.newActive(CMAgentChained.class.getName(),
                        null, node);
                agents.add(ao);

                //connect to the neighbour
                int size = agents.size();
                if (size > 1) {
                    CMAgentChained lastAgent = agents.get(size - 1);
                    CMAgentChained previousAgent = agents.get(size - 2);
                    lastAgent.setPreviousNeighbour(previousAgent);
                }
            }
            //start chained call            	
            Vector<State> states = agents.get(agents.size() / 2).getAllPreviousStates();
            for (State s : states) {
                System.out.println(s.toString());
            }

            states = agents.get(agents.size() / 2).getAllNextStates();
            for (State s : states) {
                System.out.println(s.toString());
            }

        } catch (ActiveObjectCreationException e) {
            System.err.println(e.getMessage());
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        }
    }
    //@snippet-end synch_cma_main
}