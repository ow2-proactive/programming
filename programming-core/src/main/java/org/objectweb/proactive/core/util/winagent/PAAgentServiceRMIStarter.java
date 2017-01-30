/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
            Node n = NodeFactory.createLocalNode(nodeName, false, null);
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
