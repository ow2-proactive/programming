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
package org.objectweb.proactive.examples.userguide.cmagent.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.ActiveObjectCreationException;


public class Main {
    private static GCMApplication pad;

    private static GCMVirtualNode deploy(String descriptor) {
        try {
            pad = PAGCMDeployment.loadApplicationDescriptor(new File(descriptor));
            //active all Virtual Nodes
            pad.startDeployment();
            pad.waitReady();
            //get the first Node available in the first Virtual Node 
            //specified in the descriptor file
            GCMVirtualNode vn = pad.getVirtualNode("remoteNode"); // not sure - which deployment descriptor is used ??
            return vn;
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ProActiveException proExcep) {
            System.err.println(proExcep.getMessage());
        }
        return null;
    }

    public static void main(String args[]) {
        try {
            BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
            GCMVirtualNode vn = deploy(args[0]);

            //create the active object
            CMAgentMigrator ao = (CMAgentMigrator) PAActiveObject.newActive(CMAgentMigrator.class.getName(),
                    new Object[] {}, vn.getANode());

            int k = 1;
            int choice;
            while (k != 0) {

                //display the menu with the available nodes 
                k = 1;
                for (Node node : vn.getCurrentNodes()) {
                    //TODO 2. Add the node URL to the menu 
                    System.out.println(k + ".  Statistics for node :" + node.getNodeInformation().getURL());
                    k++;
                }
                System.out.println("0.  Exit");

                Node[] nodeArray = vn.getCurrentNodes().toArray(new Node[] {});

                //select a node
                do {
                    System.out.print("Choose a node :> ");
                    try {
                        // Read am option from keyboard
                        choice = Integer.parseInt(inputBuffer.readLine().trim());
                    } catch (NumberFormatException noExcep) {
                        choice = -1;
                    }
                } while (!(choice >= 1 && choice < k || choice == 0));
                if (choice == 0)
                    break;

                //TODO 3. Migrate the active object to the selected node:  choice-1
                ao.migrateTo(nodeArray[choice - 1]); //migrate

                //TODO 4. Get the state and the last request time and print them out
                String currentState = ao.getCurrentState().toString(); //get the state
                System.out.println("\n" + currentState);
                //display information for the selected node 
                System.out.println("Calculating the statistics took " +
                    ao.getLastRequestServeTime().longValue() + "ms \n");
            }

            //stopping all the objects and JVMS
            PAActiveObject.terminateActiveObject(ao, false);
            pad.kill();
            PALifeCycle.exitSuccess();
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
