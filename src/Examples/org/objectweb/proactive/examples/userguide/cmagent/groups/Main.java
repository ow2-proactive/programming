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
//@snippet-start groups_cma_main_skeleton
package org.objectweb.proactive.examples.userguide.cmagent.groups;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.migration.CMAgentMigrator;
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

    //@snippet-start groups_cma_full
    public static void main(String args[]) {
        try {
            Vector<CMAgentMigrator> agents = new Vector<CMAgentMigrator>();
            BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
            GCMVirtualNode vn = deploy(args[0]);
            //@snippet-start groups_group_creation	
            //TODO 1. Create a new empty group
            //@snippet-break groups_cma_main_skeleton
            CMAgentMigrator monitorsGroup = (CMAgentMigrator) PAGroup.newGroup(CMAgentMigrator.class
                    .getName());
            //@snippet-resume groups_cma_main_skeleton
            //TODO 2. Create a collection of active objects with on object on each node
            //@snippet-break groups_cma_main_skeleton
            for (Node node : vn.getCurrentNodes()) {
                CMAgentMigrator ao = (CMAgentMigrator) PAActiveObject.newActive(CMAgentMigrator.class
                        .getName(), new Object[] {}, node);
                agents.add(ao);
            }
            //@snippet-resume groups_cma_main_skeleton
            //TODO 3. Get a management representation of the monitors group
            //@snippet-break groups_cma_main_skeleton
            Group<CMAgentMigrator> gA = PAGroup.getGroup(monitorsGroup);
            //@snippet-resume groups_cma_main_skeleton
            //@snippet-end groups_group_creation	
            //ask for adding or removing nodes
            //get statistics
            int k = 1;
            int choice;
            while (k != 0) {
                //display the menu 
                k = 1;
                System.out.println("Toggle monitored nodes (*) or display statistics: ");
                for (CMAgentMigrator agent : agents) {
                    //TODO 4. Print the node URL
                    if (gA.contains(agent)) {
                        //@snippet-break groups_cma_main_skeleton
                        System.out.println(" " + k + ".* " + PAActiveObject.getActiveObjectNodeUrl(agent));
                        //@snippet-resume groups_cma_main_skeleton
                    } else {
                        //@snippet-break groups_cma_main_skeleton
                        System.out.println(" " + k + ".  " + PAActiveObject.getActiveObjectNodeUrl(agent));
                        //@snippet-resume groups_cma_main_skeleton
                    }
                    k++;
                }
                System.out.println("-1.  Display statistics for monitored nodes");
                System.out.println(" 0.  Exit");

                //select a node
                do {
                    System.out.print("Choose a node to add or remove  :> ");
                    try {
                        // Read am option from keyboard
                        choice = Integer.parseInt(inputBuffer.readLine().trim());
                    } catch (NumberFormatException noExcep) {
                        choice = -1;
                    }
                } while (!(choice >= 1 && choice < k || choice == 0 || choice == -1));
                if (choice == 0)
                    break;
                if (choice == -1) {

                    State resultsGroup = monitorsGroup.getCurrentState();
                    while (PAGroup.size(resultsGroup) > 0) {
                        //@snippet-start groups_wbn
                        //TODO 5. Use PAGroup.waitAndGetOneThenRemoveIt() to control the list of State futures
                        //@snippet-break groups_cma_main_skeleton
                        State statistic = (State) PAGroup.waitAndGetOneThenRemoveIt(resultsGroup);
                        //@snippet-resume groups_cma_main_skeleton
                        //@snippet-end groups_wbn
                        System.out.println(statistic.toString());
                    }
                } else {
                    //TODO 6. Use the management representation to add or remove
                    //        the agent (choice-1) to/from the group.
                    //@snippet-break groups_cma_main_skeleton
                    //@snippet-start groups_add_remove
                    if (gA.contains(agents.elementAt(choice - 1))) {
                        gA.remove(agents.elementAt(choice - 1));
                    } else {
                        gA.add(agents.elementAt(choice - 1));
                    }
                    //@snippet-end groups_add_remove
                    //@snippet-resume groups_cma_main_skeleton
                }
            }

        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotReifiableException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (ProActiveException e) {
            System.err.println(e.getMessage());
        } finally {
            //stopping all the objects and JVMS
            if (pad != null)
                pad.kill();
            PALifeCycle.exitSuccess();
        }
    }
    //@snippet-end groups_cma_full
}
//@snippet-end groups_cma_main_skeleton
