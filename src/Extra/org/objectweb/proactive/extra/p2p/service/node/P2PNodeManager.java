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
package org.objectweb.proactive.extra.p2p.service.node;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extra.p2p.service.util.P2PConstants;


/**
 * @author The ProActive Team
 *
 * Created on Jan 12, 2005
 */
public class P2PNodeManager implements Serializable, InitActive, EndActive, P2PConstants,
        ProActiveInternalObject {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_NODES);
    private static final int PROC = Runtime.getRuntime().availableProcessors();
    private Node p2pServiceNode = null;
    private ProActiveRuntime proactiveRuntime = null;
    private ArrayList<Node> availableNodes = new ArrayList<Node>();
    private ArrayList<Node> bookedNodes = new ArrayList<Node>();
    private ArrayList<Node> usingNodes = new ArrayList<Node>();
    private int nodeCounter = 0;
    private final String descriptorPath = PAProperties.PA_P2P_XML_PATH.getValue();
    private ProActiveDescriptor pad = null;

    //--------------------------------------------------------------------------
    // Class Constructors
    //--------------------------------------------------------------------------

    /**
     * Empty constructor for new active.
     */
    public P2PNodeManager() {
        // The empty constructor
    }

    //--------------------------------------------------------------------------
    // Public Class methods
    // -------------------------------------------------------------------------

    /**
     * Asking a shared node.
     * @return a <code>P2PNode</code> which contains a node or <code>null</code>
     * if no shared nodes are available.
     */
    public P2PNode askingNode(String nodeFamilyRegexp) {
        logger.debug("Asking a node to the nodes manager");
        if ((nodeFamilyRegexp == null) || (nodeFamilyRegexp.length() == 0) ||
            System.getProperty("os.name").matches(nodeFamilyRegexp)) {
            logger.debug("Family Match");
            if ((this.availableNodes.size() == 0) && (this.bookedNodes.size() == 0) &&
                (this.usingNodes.size() == 0)) {
                this.deployingDefaultSharedNodes();
            }
            if (this.availableNodes.size() > 0) {
                Node node = (Node) this.availableNodes.remove(0);
                this.bookedNodes.add(node);
                logger.debug("Yes the manager has a node");
                return new P2PNode(node, (P2PNodeManager) PAActiveObject.getStubOnThis());
            }
        }

        // All nodes is already assigned
        logger.debug("Sorry no available node for the moment");
        return new P2PNode(null, null);
    }

    public Vector<Node> askingAllNodes(String nodeFamilyRegexp) {
        logger.debug("Asking all nodes to the nodes manager");
        if ((nodeFamilyRegexp == null) || (nodeFamilyRegexp.length() == 0) ||
            System.getProperty("os.name").matches(nodeFamilyRegexp)) {
            logger.debug("Family Match");
            if ((this.availableNodes.size() == 0) && (this.bookedNodes.size() == 0) &&
                (this.usingNodes.size() == 0)) {
                this.deployingDefaultSharedNodes();
            }
            if (this.availableNodes.size() > 0) {
                Vector<Node> allNodes = new Vector<Node>(this.availableNodes);
                this.availableNodes.clear();
                this.bookedNodes.addAll(allNodes);
                logger.debug("Yes the manager has some nodes");
                return allNodes;
            }
        }

        // All nodes is already assigned
        logger.debug("Sorry no availbale node for the moment");
        return new Vector<Node>();
    }

    public P2PNode askingNode(boolean evenIfItIsShared) {
        if (!evenIfItIsShared) {
            return askingNode(null);
        }
        logger.debug("Asking a node to the nodes manager");
        if ((this.availableNodes.size() == 0) && (this.bookedNodes.size() == 0) &&
            (this.usingNodes.size() == 0)) {
            this.deployingDefaultSharedNodes();
        }
        if (this.availableNodes.size() > 0) {
            Node node = (Node) this.availableNodes.remove(0);
            this.bookedNodes.add(node);
            logger.debug("Yes, the manager has an empty node");
            return new P2PNode(node, (P2PNodeManager) PAActiveObject.getStubOnThis());
        } else if (this.bookedNodes.size() > 0) {
            Node node = this.bookedNodes.get(0);
            logger.debug("Yes, the manager has a shared node");
            return new P2PNode(node, (P2PNodeManager) PAActiveObject.getStubOnThis());
        } else {
            // All nodes is already assigned
            logger.debug("Sorry no availbale node for the moment");
            return new P2PNode(null, null);
        }
    }

    /**
     * Leave the specified node. The node is killed and new one is created and
     * ready for sharing.
     * @param nodeToFree the node to kill.
     * @param vnName Virtual node name to unregister or null.
     */
    public void leaveNode(Node nodeToFree, String vnName) {
        String nodeUrl = nodeToFree.getNodeInformation().getURL();
        logger.debug("LeaveNode message received for node @" + nodeUrl);

        boolean found = false;
        Iterator<Node> it = this.usingNodes.iterator();
        while (it.hasNext()) {
            Node node = it.next();
            if (node.getNodeInformation().getURL().equals(nodeUrl)) {
                usingNodes.remove(node);
                found = true;
                logger.debug("node removed from used list: " + nodeUrl);
                break;
            }
        }
        if (!found) {
            logger.error("A remote peer has given back an unknown node !!!!!");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("using size: " + this.usingNodes.size() + " booked size: " +
                this.bookedNodes.size() + " available size: " + this.availableNodes.size());
        }

        try {
            // Kill the node
            if (this.descriptorPath == null) {
                this.proactiveRuntime.killNode(URIBuilder.getNameFromURI(nodeUrl));
                logger.info("Node @" + nodeUrl + " left");
                // Creating a new node
                this.createNewNode();
            } else {
                this.availableNodes.add(nodeToFree);
            }
        } catch (Exception e) {
            logger.fatal("Couldn't delete or create a shared node", e);
        }
    }

    /**
     * Free a booked node.
     * @param givenNode node given and not used.
     */
    public void noMoreNodeNeeded(Node givenNode) {
        logger.debug("noMoreNeeded() start " + givenNode);
        String nodeURL = givenNode.getNodeInformation().getURL();

        boolean found = false;
        Iterator<Node> it = this.bookedNodes.iterator();
        while (it.hasNext()) {
            Node current = it.next();
            logger.debug("Analyzing: " + current.getNodeInformation().getURL());
            if (current.getNodeInformation().getURL().equals(nodeURL)) {
                logger.debug("noMoreNeeded() match");
                this.bookedNodes.remove(current);
                this.availableNodes.add(current);
                if (logger.isInfoEnabled()) {
                    logger.info("Booked node " + nodeURL + " is now shared");
                }
                found = true;
                break;
            }
        }
        if (!found) {
            logger.error("A remote peer no more need an unknown node !!!!!");
        }
    }

    //--------------------------------------------------------------------------
    // Active Object methods
    //--------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        logger.debug("Entering initActivity");

        try {
            // Getting reference to the P2P node
            this.p2pServiceNode = NodeFactory.getNode(body.getNodeURL());
            // Getting ProActive runtime
            this.proactiveRuntime = this.p2pServiceNode.getProActiveRuntime();
        } catch (NodeException e) {
            logger.fatal("Couldn't get reference to the local p2pServiceNode", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("P2P node manager is running at " +
                this.p2pServiceNode.getNodeInformation().getURL());
            logger.debug("ProActiveRuntime at " + this.proactiveRuntime.getURL());
        }

        // Creating shared nodes
        if (this.descriptorPath == null) {
            this.deployingDefaultSharedNodes();
        } else {
            this.deployingXmlSharedNodes();
        }

        logger.debug("Exiting initActivity");
    }

    /**
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
        if (this.pad != null) {
            try {
                this.pad.killall(false);
            } catch (ProActiveException e) {
                logger.warn("Couldn't kill deployed nodes", e);
            }
        }
    }

    /**
     * Use the node means acknowledge that a given node is "busy" after being "booked"
     */

    public boolean useNode(Node n) {
        logger.debug("booked node is now used : " + n.getNodeInformation().getURL());
        Iterator<Node> it = bookedNodes.iterator();
        while (it.hasNext()) {
            Node booking = it.next();
            if (booking.getNodeInformation().getURL().equals(n.getNodeInformation().getURL())) {
                bookedNodes.remove(booking);
                usingNodes.add(booking);
                logger.debug("node moved from booked list to used list: " + n.getNodeInformation().getURL());
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Private class method
    // -------------------------------------------------------------------------

    /**
     * @return a new shared node.
     * @throws NodeException
     * @throws ProActiveException
     * @throws AlreadyBoundException
     */
    private Node createNewNode() throws NodeException, ProActiveException, AlreadyBoundException {
        // security 
        ProActiveSecurityManager newNodeSecurityManager = null;

        newNodeSecurityManager = ((AbstractBody) PAActiveObject.getBodyOnThis())
                .getProActiveSecurityManager();

        if (newNodeSecurityManager != null) {
            System.out.println(newNodeSecurityManager);
            newNodeSecurityManager = newNodeSecurityManager.generateSiblingCertificate(EntityType.NODE,
                    P2PConstants.VN_NAME);
        }

        Node newNode = NodeFactory.createLocalNode(P2PConstants.SHARED_NODE_NAME + "_" + this.nodeCounter++,
                true, newNodeSecurityManager, P2PConstants.VN_NAME, null);
        this.availableNodes.add(newNode);
        logger.info("New shared node created @" + newNode.getNodeInformation().getURL());
        return newNode;
    }

    /**
     * Starting default shared nodes. One by processor.
     */
    private void deployingDefaultSharedNodes() {
        assert PROC > 0 : "Processor count = 0";
        logger.debug("Number of available processors for this JVM: " + PROC);
        int nodes = PROC;
        if (!PAProperties.PA_P2P_MULTI_PROC_NODES.isTrue()) {
            nodes = 1;
        }

        // No sharing enable
        if (PAProperties.PA_P2P_NO_SHARING.isTrue()) {
            nodes = 0;
        }

        // Starting default shared nodes
        for (int procNumber = 0; procNumber < nodes; procNumber++) {
            try {
                Node node = this.createNewNode();
                logger.debug("Default shared node succefuly created at: " +
                    node.getNodeInformation().getURL());
            } catch (Exception e) {
                logger.warn("Couldn't create default shared node", e);
            }
        }
        logger.debug(nodes + " shared nodes deployed");
    }

    /**
     * Deploying shred nodes from a XML descriptor
     */
    private void deployingXmlSharedNodes() {
        try {
            this.pad = PADeployment.getProactiveDescriptor(this.descriptorPath);
        } catch (ProActiveException e) {
            logger.fatal("Could't get ProActive Descripor at " + this.descriptorPath, e);
            return;
        }
        VirtualNode[] virtualNodes = this.pad.getVirtualNodes();
        this.pad.activateMappings();
        for (int i = 0; i < virtualNodes.length; i++) {
            VirtualNode currentVn = virtualNodes[i];
            Node[] nodes;
            try {
                nodes = currentVn.getNodes();
                for (int j = 0; j < nodes.length; j++) {
                    this.availableNodes.add(nodes[j]);
                }
            } catch (NodeException e) {
                logger.warn("Problem with nodes for " + currentVn.getName(), e);
            }
        }

        // Killing deployed nodes at the JVM shutdown
        XmlNodeKiller killer = new XmlNodeKiller(pad);
        Runtime.getRuntime().addShutdownHook(new Thread(killer));

        logger.info(this.availableNodes.size() + " shared nodes deployed");
    }
}
