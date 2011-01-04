/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
//@tutorial-start
package org.objectweb.proactive.examples.userguide.cmagent.synch;

import java.io.File;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.simple.State;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Main {
    private static GCMApplication pad;

    private static GCMVirtualNode deploy(String descriptor) throws NodeException, ProActiveException {

        //Create object representation of the deployment file
        pad = PAGCMDeployment.loadApplicationDescriptor(new File(descriptor));
        //Activate all Virtual Nodes
        pad.startDeployment();
        //Wait for all the virtual nodes to become ready
        pad.waitReady();

        //Get the first Virtual Node specified in the descriptor file
        GCMVirtualNode vn = pad.getVirtualNodes().values().iterator().next();

        return vn;
    }

    //@snippet-start synch_cma_main
    public static void main(String args[]) {
        try {
            GCMVirtualNode vn = deploy(args[0]);
            Vector<CMAgentChained> agents = new Vector<CMAgentChained>();
            //create the active objects
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
        } catch (ProActiveException e) {
            System.err.println(e.getMessage());
        } finally {
            //stopping all the objects and JVMS
            if (pad != null)
                pad.kill();
            PALifeCycle.exitSuccess();
        }
    }
    //@snippet-end synch_cma_main
}
//@tutorial-end
