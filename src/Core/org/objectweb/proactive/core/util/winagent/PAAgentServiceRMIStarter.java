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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.util.winagent;

import java.rmi.AlreadyBoundException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;


public class PAAgentServiceRMIStarter {

    /**
     * This class is responsible for implementing actions that are started in
     * ProActiveAgent: - advertisement in RMI Registry
     * 
     * The created process from this class should be monitored by ProActiveAgent
     * component and restarted automatically on any failures
     */

    private static final String PAAGENT_NODE_NAME = "PA-AGENT_NODE";

    // command dispatch

    public static void main(String args[]) {
        String nodeName = PAAGENT_NODE_NAME;
        if (args.length > 0)
            nodeName = args[0];
        doAdvert(nodeName);
    }

    // registers local node in RMI registry

    private static void doAdvert(String nodeName) {
        try {
            // TODO: localhost?
            Node n = NodeFactory.createLocalNode(nodeName, false, null, null, null);
            System.out.println("The node was registered at " + n.getNodeInformation().getURL());
        } catch (ProActiveException e) {
            e.printStackTrace();
            return;
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            return;
        }

    }

}
