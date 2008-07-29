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
package org.objectweb.proactive.extra.p2p.service.messages;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.p2p.service.P2PService;
import org.objectweb.proactive.extra.p2p.service.node.P2PLookupInt;
import org.objectweb.proactive.extra.p2p.service.node.P2PNode;
import org.objectweb.proactive.extra.p2p.service.node.P2PNodeAck;
import org.objectweb.proactive.extra.p2p.service.util.P2PConstants;
import org.objectweb.proactive.extra.p2p.service.util.UniversalUniqueID;


public class RequestSingleNodeMessage extends RandomWalkMessage {
    protected String vnName;
    protected String jobId;
    protected P2PLookupInt lookup;
    protected Boolean active;

    public RequestSingleNodeMessage(int ttl, UniversalUniqueID uuid, P2PService service, P2PLookupInt lookup,
            String vnName, String jobId) {
        this.TTL = ttl;
        this.uuid = uuid;
        this.sender = service;
        this.vnName = vnName;
        this.jobId = jobId;
        this.lookup = lookup;
        this.active = true;
    }

    @Override
    public void execute(P2PService target) {

        P2PNode askedNode = target.getNodeManager().askingNode(null);
        Node nodeAvailable = askedNode.getNode();
        if (nodeAvailable != null) {
            P2PNodeAck nodeAck = null;
            try {
                nodeAck = lookup.giveNode(nodeAvailable, askedNode.getNodeManager());
                this.active = false;
            } catch (Exception lookupExcption) {
                logger.info("Cannot contact the remote lookup", lookupExcption);
                target.getNodeManager().noMoreNodeNeeded(nodeAvailable);
                return;
            }

            long endTime = System.currentTimeMillis() + P2PService.ACQ_TO;
            while ((System.currentTimeMillis() < endTime) && PAFuture.isAwaited(nodeAck)) {
                target.service.blockingServeOldest(2000);
            }
            if (PAFuture.isAwaited(nodeAck)) {
                // Do not forward the message, Prevent  deadlock
                target.getNodeManager().noMoreNodeNeeded(nodeAvailable);
                return;
            }
            // Waiting ACK or NACK
            if (nodeAck.ackValue()) {
                // Setting vnInformation and JobId
                if (vnName != null) {
                    try {
                        nodeAvailable.getProActiveRuntime().registerVirtualNode(vnName, true);
                    } catch (Exception e) {
                        logger.warn("Couldn't register " + vnName + " in the PAR", e);
                    }
                }
                if (jobId != null) {
                    nodeAvailable.getNodeInformation().setJobID(jobId);
                }
                logger.info("Giving 1 node to vn: " + vnName);
                target.getNodeManager().useNode(nodeAvailable);
            } else {
                // It's a NACK node
                target.getNodeManager().noMoreNodeNeeded(nodeAvailable);
                logger.debug("NACK node received");
                // No more nodes needed
                return;
            }
        }
    }

    @Override
    public boolean shouldExecute() {
        return active;
    }

    @Override
    public boolean shouldTransmit() {
        return active;
    }
}
