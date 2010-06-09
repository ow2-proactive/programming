/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.core.component.adl.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * @author The ProActive Team
 */
public class ADLNodeProvider {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);
    public static final String NODES_ID = "nodes";
    private static final int TIMEOUT = 60000;
    private static Map<String, List<Node>> nodeLists = new HashMap<String, List<Node>>();
    private static Map<String, Integer> nodeIndex = new HashMap<String, Integer>();

    public static Node getNode(Object nodesContainer) throws Exception {
        if (nodesContainer instanceof org.objectweb.proactive.core.descriptor.data.VirtualNode) {
            return getNode((org.objectweb.proactive.core.descriptor.data.VirtualNode) nodesContainer);
        } else if (nodesContainer instanceof GCMVirtualNode) {
            return getNode((GCMVirtualNode) nodesContainer);
        } else if (nodesContainer instanceof List<?>) {
            return getNode(getNodeList(nodesContainer));
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Node> getNodeList(Object nodesContainer) {
        return (List<Node>) nodesContainer;
    }

    public static Node getNode(org.objectweb.proactive.core.descriptor.data.VirtualNode vn) throws Exception {
        if (vn != null) {
            try {
                waitReady(vn);
                return vn.getNode();
            } catch (NodeException ne) {
                InstantiationException ie = new InstantiationException(
                    "Cannot instantiate component due to a deployment problem : " + ne.getMessage());
                ie.initCause(ne);
                throw ie;
            }
        } else {
            return null;
        }
    }

    public static void waitReady(org.objectweb.proactive.core.descriptor.data.VirtualNode vn)
            throws Exception {
        vn.activate();
        if (vn.getNodes().length == 0) {
            throw new InstantiationException(
                "Cannot create component on virtual node as no node is associated with this virtual node");
        }
    }

    public static Node getNode(GCMVirtualNode gcmVn) throws Exception {
        if (gcmVn != null) {
            String gcmVnName = gcmVn.getName();
            if (nodeLists.containsKey(gcmVnName)) {
                List<Node> curNodeList = nodeLists.get(gcmVnName);
                int curNodeIndex = nodeIndex.get(gcmVnName);
                if (curNodeIndex < curNodeList.size()) {
                    Node result = curNodeList.get(curNodeIndex);
                    nodeIndex.put(gcmVnName, ++curNodeIndex);
                    return result;
                } else {
                    waitReady(gcmVn);
                    List<Node> newNodeList = gcmVn.getCurrentNodes();
                    if (newNodeList.equals(curNodeList)) {
                        nodeIndex.put(gcmVnName, 0);
                    } else {
                        nodeLists.put(gcmVnName, newNodeList);
                    }
                    return getNode(gcmVn);
                }
            } else {
                waitReady(gcmVn);
                nodeLists.put(gcmVnName, gcmVn.getCurrentNodes());
                nodeIndex.put(gcmVnName, 0);
                return getNode(gcmVn);
            }
        } else {
            return null;
        }
    }

    public static void waitReady(GCMVirtualNode gcmVn) throws Exception {
        boolean waiting = true;
        while (waiting) {
            try {
                gcmVn.waitReady(TIMEOUT);
                waiting = false;
            } catch (ProActiveTimeoutException pate) {
                logger.warn("The virtual node: " + gcmVn.getName() +
                    " is still not ready after having waited " + (TIMEOUT / 1000) +
                    " seconds. Awaiting further " + (TIMEOUT / 1000) + " seconds.");
            }
        }
        if (gcmVn.getNbCurrentNodes() == 0) {
            throw new InstantiationException("Cannot create component on virtual node " + gcmVn.getName() +
                " as no node is associated with this virtual node");
        }
    }

    public static Node getNode(List<Node> nodes) throws Exception {
        if (nodes != null) {
            if (nodeLists.containsKey(NODES_ID)) {
                List<Node> curNodeList = nodeLists.get(NODES_ID);
                if (!curNodeList.equals(nodes)) {
                    nodeLists.remove(NODES_ID);
                    nodeIndex.remove(NODES_ID);
                    return getNode(nodes);
                } else {
                    int curNodeIndex = nodeIndex.get(NODES_ID);
                    if (curNodeIndex < curNodeList.size()) {
                        Node result = curNodeList.get(curNodeIndex);
                        nodeIndex.put(NODES_ID, ++curNodeIndex);
                        return result;
                    } else {
                        nodeIndex.put(NODES_ID, 0);
                        return getNode(nodes);
                    }
                }
            } else {
                if (nodes.size() == 0) {
                    logger
                            .info("An empty list of nodes has been set in the context, component will be instantiated in the current virtual machine");
                    return null;
                }
                nodeLists.put(NODES_ID, nodes);
                nodeIndex.put(NODES_ID, 0);
                return getNode(nodes);
            }
        } else {
            return null;
        }
    }
}
