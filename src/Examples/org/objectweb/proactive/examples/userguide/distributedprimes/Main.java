/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.userguide.distributedprimes;

import java.io.File;
import java.util.Map;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.api.PAActiveObject;


public class Main {
    private static Map<String, GCMVirtualNode> deploy(String descriptor) {
        GCMApplication pad;
        try {
            pad = PAGCMDeployment.loadApplicationDescriptor(new File(descriptor));
            //active all Virtual Nodes
            pad.startDeployment();
            pad.waitReady();
            //get the first Node available in the first Virtual Node 
            //specified in the descriptor file
            return pad.getVirtualNodes();
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ProActiveException proExcep) {
            System.err.println(proExcep.getMessage());
        }
        return null;
    }

    public static void main(String args[]) {
        try {
            Map<String, GCMVirtualNode> listOfVN = deploy(args[0]);
            GCMVirtualNode masterNode = listOfVN.get("remoteNode1");

            //create the active object on the first node on
            //the first virtual node available
            //start the master
            PrimeManager master = (PrimeManager) PAActiveObject.newActive(PrimeManager.class.getName(),
                    new Object[] {}, masterNode.getANode());

            //iterate through all the nodes and deploy
            //a worker on the first node on each VN available
            Node node;
            PrimeWorker worker;
            for (GCMVirtualNode vn : listOfVN.values()) {
                node = vn.getANode();
                //deploy
                worker = (PrimeWorker) PAActiveObject.newActive(PrimeWorker.class.getName(), new Object[] {},
                        node);
                master.addWorker(worker);
            }
            //tell the master to start
            master.startComputation(Long.parseLong(args[1]));
            //listOfVN[0].killAll(true);
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        } catch (NumberFormatException nrExcep) {
            System.out.println("Number format is wrong !");
            System.err.println(nrExcep.getMessage());
            return;
        }
        //quitting
    }
}