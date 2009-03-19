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
package org.objectweb.proactive.core.body.ft.servers.resource;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 * @since 2.2
 */
public class ResourceServerImpl implements ResourceServer {

    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE);

    // global server
    private FTServer server;

    // list of free ProActiveRuntime
    private List<Node> freeNodes;

    // number of returned free nodes
    private int nodeCounter;

    public ResourceServerImpl(FTServer server) {
        this.server = server;
        this.freeNodes = new ArrayList<Node>();
        this.nodeCounter = 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer#addFreeNode(org.objectweb.proactive.core.node.Node)
     */
    public void addFreeNode(Node n) throws RemoteException {
        logger.info("[RESSOURCE] A node is added : " + n.getNodeInformation().getURL());
        this.freeNodes.add(n);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer#getFreeNode()
     */
    public Node getFreeNode() throws RemoteException {
        this.nodeCounter++;
        Node n = null;
        if (this.freeNodes.isEmpty()) {
            logger.error("[RESSOURCE] **ERROR** There is no resource nodes !");
            return null;
        } else {
            n = (this.freeNodes.get(nodeCounter % (this.freeNodes.size())));
        }
        try {
            // testing free node
            n.getNumberOfActiveObjects();
        } catch (NodeException e) {
            // free node is unreachable !
            logger.info("[RESSOURCE] An unreachable node is removed.");
            this.freeNodes.remove(n);
            this.nodeCounter = 0;
            n = getFreeNode();
        }
        logger.info("[RESSOURCE] Return a node : " + n.getNodeInformation().getURL());
        return n;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer#initialize()
     */
    public void initialize() throws RemoteException {
        this.freeNodes = new ArrayList<Node>();
        this.nodeCounter = 0;
    }
}
