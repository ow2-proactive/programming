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
package org.objectweb.proactive.core.component.adl.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * @author The ProActive Team
 */
public class ADLNodeProvider {
    private static Map<String, List<Node>> nodeLists = new HashMap<String, List<Node>>();
    private static Map<String, Integer> nodeIndex = new HashMap<String, Integer>();

    public static Node getNode(GCMVirtualNode gcmDeploymentVN) throws Exception {
        if (gcmDeploymentVN != null) {
            String gcmVNName = gcmDeploymentVN.getName();
            if (nodeLists.containsKey(gcmVNName)) {
                List<Node> curNodeList = nodeLists.get(gcmVNName);
                int curNodeIndex = nodeIndex.get(gcmVNName);
                if (curNodeIndex < curNodeList.size()) {
                    Node result = curNodeList.get(curNodeIndex);
                    nodeIndex.put(gcmVNName, ++curNodeIndex);
                    return result;
                } else {
                    List<Node> newNodeList = gcmDeploymentVN.getCurrentNodes();
                    if (newNodeList.size() == curNodeList.size()) {
                        nodeIndex.put(gcmVNName, 0);
                    } else {
                        nodeLists.put(gcmVNName, newNodeList);
                    }
                    return getNode(gcmDeploymentVN);
                }
            } else {
                if (gcmDeploymentVN.getNbCurrentNodes() == 0) {
                    throw new InstantiationException("Cannot create component on virtual node " + gcmVNName +
                        " as no node is associated with this virtual node");
                }
                nodeLists.put(gcmVNName, gcmDeploymentVN.getCurrentNodes());
                nodeIndex.put(gcmVNName, 0);
                return getNode(gcmDeploymentVN);
            }
        } else {
            return null;
        }
    }
}
