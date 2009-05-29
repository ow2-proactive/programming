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
//@snippet-start cma_deploy_full
//@snippet-start deploy_CMA_skeleton
package org.objectweb.proactive.examples.userguide.cmagent.deployed;

import java.io.File;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Main {
    private static GCMApplication pad;

    //@snippet-start cma_deploy_method
    //deployment method
    private static GCMVirtualNode deploy(String descriptor) throws NodeException, ProActiveException {
        //TODO 1. Create object representation of the deployment file
        //@snippet-break deploy_CMA_skeleton
        //@tutorial-break
        pad = PAGCMDeployment.loadApplicationDescriptor(new File(descriptor));
        //@tutorial-resume
        //@snippet-resume deploy_CMA_skeleton
        //TODO 2. Activate all Virtual Nodes
        //@snippet-break deploy_CMA_skeleton
        //@tutorial-break
        pad.startDeployment();
        //@tutorial-resume
        //@snippet-resume deploy_CMA_skeleton
        //TODO 3. Wait for all the virtual nodes to become ready
        //@snippet-break deploy_CMA_skeleton
        //@tutorial-break
        pad.waitReady();
        //@tutorial-resume
        //@snippet-resume deploy_CMA_skeleton
        //TODO 4. Get the first Virtual Node specified in the descriptor file
        //@snippet-break deploy_CMA_skeleton
        //@tutorial-break
        GCMVirtualNode vn = pad.getVirtualNodes().values().iterator().next();
        //@tutorial-resume
        //@snippet-resume deploy_CMA_skeleton
        //TODO 5. Return the virtual node
        //@snippet-break deploy_CMA_skeleton
        //@tutorial-break
        return vn;
        //@tutorial-resume
        //@snippet-resume deploy_CMA_skeleton
    }

    //@snippet-end cma_deploy_method

    public static void main(String args[]) {
        try {
            //TODO 6. Get the virtual node through the deploy method
            //@snippet-break deploy_CMA_skeleton
            //@tutorial-break
            GCMVirtualNode vn = deploy(args[0]);
            //@tutorial-resume
            //@snippet-resume deploy_CMA_skeleton
            //@snippet-start cma_deploy_object
            //TODO 7. Create the active object using a node on the virtual node
            //@snippet-break deploy_CMA_skeleton
            //@tutorial-break
            CMAgentInitialized ao = (CMAgentInitialized) PAActiveObject.newActive(CMAgentInitialized.class
                    .getName(), new Object[] {}, vn.getANode());
            //@tutorial-resume
            //@snippet-resume deploy_CMA_skeleton
            //@snippet-end cma_deploy_object
            //TODO 8. Get the current state from the active object
            //@snippet-break deploy_CMA_skeleton
            //@tutorial-break
            String currentState = ao.getCurrentState().toString();
            //@tutorial-resume
            //@snippet-resume deploy_CMA_skeleton
            //TODO 9. Print the state
            //@snippet-break deploy_CMA_skeleton
            //@tutorial-break
            System.out.println(currentState);
            //@tutorial-resume
            //@snippet-resume deploy_CMA_skeleton
            //TODO 10. Stop the active object
            //@snippet-break deploy_CMA_skeleton
            //@tutorial-break
            PAActiveObject.terminateActiveObject(ao, false);
            //@tutorial-resume
            //@snippet-resume deploy_CMA_skeleton

        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        } catch (ProActiveException poExcep) {
            System.err.println(poExcep.getMessage());
        } finally {
            //TODO 11. Stop the virtual node
            //@snippet-break deploy_CMA_skeleton
            //@tutorial-break
            if (pad != null)
                pad.kill();
            PALifeCycle.exitSuccess();
            //@tutorial-resume
            //@snippet-resume deploy_CMA_skeleton
        }
    }
}
//@tutorial-end
//@snippet-end deploy_CMA_skeleton
//@snippet-end cma_deploy_full