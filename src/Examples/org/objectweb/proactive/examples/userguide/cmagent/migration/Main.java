//@tutorial-start
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
//@snippet-start migrate_main_cma_skeleton
//@snippet-start migrate_main_cma_full
package org.objectweb.proactive.examples.userguide.cmagent.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.ActiveObjectCreationException;


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
                    //@snippet-break migrate_main_cma_skeleton
                    //@tutorial-break
                    System.out.println(k + ".  Statistics for node :" + node.getNodeInformation().getURL());
                    //@tutorial-resume
                    //@snippet-resume migrate_main_cma_skeleton
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
                //@snippet-break migrate_main_cma_skeleton
                //@tutorial-break
                ao.migrateTo(nodeArray[choice - 1]); //migrate
                //@tutorial-resume
                //@snippet-resume migrate_main_cma_skeleton
                //TODO 4. Get the state and the last request time and print them out
                //@snippet-break migrate_main_cma_skeleton
                //@tutorial-break
                String currentState = ao.getCurrentState().toString(); //get the state
                System.out.println("\n" + currentState);
                //@tutorial-resume
                //@snippet-resume migrate_main_cma_skeleton
                //TODO 5. Print the execution time of the last request
                //@snippet-break migrate_main_cma_skeleton
                //@tutorial-break
                System.out.println("Calculating the statistics took " +
                    ao.getLastRequestServeTime().longValue() + "ms \n");
                //@tutorial-resume
                //@snippet-resume migrate_main_cma_skeleton
            }
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ProActiveException e) {
            System.err.println(e.getMessage());
        } finally {
            //TODO 6. Stop all the objects and JVM
            //@snippet-break migrate_main_cma_skeleton
            //@tutorial-break
            if (pad != null)
                pad.kill();
            PALifeCycle.exitSuccess();
            //@snippet-resume migrate_main_cma_skeleton
            //@tutorial-resume
        }
    }
}
//@snippet-end migrate_main_cma_skeleton
//@snippet-end migrate_main_cma_full
//@tutorial-end