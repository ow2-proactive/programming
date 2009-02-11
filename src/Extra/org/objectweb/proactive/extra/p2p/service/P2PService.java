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
package org.objectweb.proactive.extra.p2p.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extra.p2p.service.exception.P2POldMessageException;
import org.objectweb.proactive.extra.p2p.service.exception.PeerDoesntExist;
import org.objectweb.proactive.extra.p2p.service.messages.DumpAcquaintancesMessage;
import org.objectweb.proactive.extra.p2p.service.messages.ExplorationMessage;
import org.objectweb.proactive.extra.p2p.service.messages.Message;
import org.objectweb.proactive.extra.p2p.service.node.P2PNodeLookup;
import org.objectweb.proactive.extra.p2p.service.node.P2PNodeManager;
import org.objectweb.proactive.extra.p2p.service.util.P2PConstants;
import org.objectweb.proactive.extra.p2p.service.util.UniversalUniqueID;


/**
 * <p>ProActive Peer-to-Peer Service.</p>
 * <p>This class is made to be actived.</p>
 *
 *
 *
 */
@PublicAPI
@SuppressWarnings("serial")
@ActiveObject
public class P2PService implements InitActive, P2PConstants, Serializable {

    /** Logger. */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SERVICE);

    /**
     * Stub of active object P2PAcquaintanceManager  
     */
    private P2PAcquaintanceManager acquaintanceManager;

    /**
     * Reference to the current Node.
     */
    private Node p2pServiceNode = null;

    static {
        ProActiveConfiguration.load();
    }

    private static final int MSG_MEMORY = (PAProperties.PA_P2P_MSG_MEMORY.getValue() == null) ? 10000
            : Integer.parseInt(PAProperties.PA_P2P_MSG_MEMORY.getValue());

    private static final int EXPL_MSG = Integer.parseInt(PAProperties.PA_P2P_EXPLORING_MSG.getValue()) - 1;

    /**
     * Node acquisition request timeout 
     */
    static public final long ACQ_TO = Long.parseLong(PAProperties.PA_P2P_NODES_ACQUISITION_T0.getValue());

    static final long TTU = Long.parseLong(PAProperties.PA_P2P_TTU.getValue());

    static final int TTL = Integer.parseInt(PAProperties.PA_P2P_TTL.getValue());

    /**
     * Sequence number list of received messages.
     */
    private Vector<UniversalUniqueID> oldMessageList = new Vector<UniversalUniqueID>();

    /**
     * Local Nodes manager 
     */
    private P2PNodeManager nodeManager = null;

    /**
     * A collection of not full <code>P2PNodeLookup</code>.
     */
    private Vector<P2PNodeLookup> waitingNodesLookup = new Vector<P2PNodeLookup>();
    private Vector<P2PNodeLookup> waitingMaximunNodesLookup = new Vector<P2PNodeLookup>();

    /**
     * Stub of Local P2PService (stub of itself) 
     */
    private P2PService stubOnThis = null;

    public P2PService getStubOnThis() {
        return stubOnThis;
    }

    public void setStubOnThis(P2PService stubOnThis) {
        this.stubOnThis = stubOnThis;
    }

    /**
     * Service of AO
     */
    private Service service = null;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    /**
     * Starting list of super peers.
     */

    private Vector<String> superPeers = new Vector<String>();

    /**
     * Filter of request received on active object.
     */
    private RequestFilter filter = new RequestFilter() {

        /**
         * @see org.objectweb.proactive.core.body.request.RequestFilter#acceptRequest(org.objectweb.proactive.core.body.request.Request)
         */
        public boolean acceptRequest(Request request) {
            String requestName = request.getMethodName();
            if (requestName.compareToIgnoreCase("askingNode") == 0) {
                return false;
            }
            return true;
        }
    };

    public RequestFilter getRequestFilter() {
        return filter;
    }

    public void setRequestFilter(RequestFilter filter) {
        this.filter = filter;
    }

    /**
     * Build from an URL, a URL made of host name and host port
     * @param url source URL
     * @return an URL
     */
    public static String getHostNameAndPortFromUrl(String url) {
        return URIBuilder.getHostNameFromUrl(url) + ":" + URIBuilder.getPortNumber(url);
    }

    //--------------------------------------------------------------------------
    // Class Constructors
    //--------------------------------------------------------------------------

    /**
     * The empty constructor.
     *
     * @see org.objectweb.proactive.api.PAActiveObject
     */
    public P2PService() {
    }

    /**
     * Constructor. Build P2PService object, with an initial
     * list of peers. 
     * @param superPeers
     */
    public P2PService(Vector<String> superPeers) {
        this.superPeers = superPeers;
    }

    //--------------------------------------------------------------------------
    // Public Class methods
    // -------------------------------------------------------------------------

    /**
     * Contact all specified peers to enter in the existing P2P network.
     * @param peers a list of peers URL.
     */
    public void firstContact(Vector<String> peers) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>>>>>>>>>>>>>>>> Have to contact: " + peers.size());
        }
        this.acquaintanceManager.setPreferedAcq(peers);
    }

    /**
     * Just to test if the peer is alive.
     */
    public void heartBeat() {
        if (logger.isDebugEnabled()) {
            logger.debug("Heart-beat message received");
        }
    }

    /**
     * Generate a dumpAcquaintance message and call
     *  {@link #dumpAcquaintances(Message)}
     */
    public void dumpAcquaintances() {
        DumpAcquaintancesMessage m = new DumpAcquaintancesMessage(10, this.generateUuid(), this.stubOnThis);
        this.dumpAcquaintances(m);
    }

    /**
     * Execute the DumpAcquaintance Message
     * @param m
     */
    public void dumpAcquaintances(Message m) {
        m.setSender(this.stubOnThis);
        this.isAnOldMessage(m.getUuid());
        //execute locally
        m.execute(this);
        //start the flooding
        //  m.transmit(this.acquaintanceManager_active.getAcquaintances());
        m.transmit(this);
    }

    /**
     * Start the exploration process
     * Build an exploration message and send it to the current acquaintances
     *
     */
    public void explore() {
        ExplorationMessage m = new ExplorationMessage(10, this.generateUuid(), this.stubOnThis);
        // m.transmit(this.acquaintanceManager_active.getAcquaintances());
        //		this.acquaintanceManager_active.transmit(m);
        m.transmit(this);
    }

    /**
     * Call By P2PNodeLookup
     * Treat a Booking node's request.
     * @param m
     */
    public void requestNodes(Message m) {
        m.execute(this);
        if (logger.isDebugEnabled()) {
            logger.debug("AFTER EXECUTE");
        }
        try {
            if (shouldTransmit(m)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("SHOULD TRANSMIT");
                }
                m.transmit(this);
            }
        } catch (P2POldMessageException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("P2PService.requestNodes()");
            }
        }
    }

    /**
     * Perform message treatment.
     * @param message
     */
    public void message(Message message) {
        UniversalUniqueID uuid = message.getUuid();
        int ttl = message.getTTL();
        if (uuid != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Message " + message + "  received with #" + uuid);
            }
            ttl--;
            message.setTTL(ttl);
        }

        boolean transmit;
        try {
            transmit = shouldTransmit(message);
        } catch (P2POldMessageException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("P2PService.message() received an old message");
            }
            return;
        }
        if (shouldExecute(message)) {
            message.execute(this);
        }
        if (transmit) {
            //    message.transmit(this.acquaintanceManager_active.getAcquaintances());
            //			this.acquaintanceManager_active.transmit(message);
            message.transmit(this);
        }
    }

    private boolean shouldExecute(Message message) {
        return message.shouldExecute();
    }

    /** Put in a <code>P2PNodeLookup</code>, the number of asked nodes.
     * @param numberOfNodes the number of asked nodes.
     * @param nodeFamilyRegexp the regexp for the famili, null or empty String for all.
     * @param vnName Virtual node name.
     * @param jobId of the vn.
     * @return the number of asked nodes.
     */
    public P2PNodeLookup getNodes(int numberOfNodes, String nodeFamilyRegexp, String vnName, String jobId) {
        Object[] params = new Object[5];
        params[0] = new Integer(numberOfNodes);
        params[1] = this.stubOnThis;
        params[2] = vnName;
        params[3] = jobId;
        params[4] = nodeFamilyRegexp;

        P2PNodeLookup lookup_active = null;
        try {
            lookup_active = (P2PNodeLookup) PAActiveObject.newActive(P2PNodeLookup.class.getName(), params,
                    this.p2pServiceNode);
            PAActiveObject.enableAC(lookup_active);
            if (numberOfNodes == MAX_NODE) {
                this.waitingMaximunNodesLookup.add(lookup_active);
            } else {
                this.waitingNodesLookup.add(lookup_active);
            }
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't create an active lookup", e);
            return P2PNodeLookup.INVALID_NODE_LOOKUP;
        } catch (NodeException e) {
            logger.fatal("Couldn't connect node to creat", e);
            return P2PNodeLookup.INVALID_NODE_LOOKUP;
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Couldn't enable AC for a nodes lookup", e);
            }
        }

        if (logger.isInfoEnabled()) {
            if (numberOfNodes != MAX_NODE) {
                logger.info("Asking for " + numberOfNodes + " nodes");
            } else {
                logger.info("Asking for maxinum nodes");
            }
        }
        return lookup_active;
    }

    /** Put in a <code>P2PNodeLookup</code>, the number of asked nodes.
     * @param numberOfNodes the number of asked nodes.
     * @param vnName Virtual node name.
     * @param jobId of the vn.
     * @return the number of asked nodes.
     */
    public P2PNodeLookup getNodes(int numberOfNodes, String vnName, String jobId) {
        return this.getNodes(numberOfNodes, ".*", vnName, jobId);
    }

    /**
     * Put in a <code>P2PNodeLookup</code> all available nodes during all the
     * time where it is actived.
     * @param vnName Virtual node name.
     * @param jobId
     * @return an active object where nodes are received.
     */
    public P2PNodeLookup getMaximunNodes(String vnName, String jobId) {
        return this.getNodes(P2PConstants.MAX_NODE, vnName, jobId);
    }

    /**
     * For load balancing.
     * @return URL of the node where the P2P service is running.
     */
    public StringWrapper getAddress() {
        return new StringWrapper(this.p2pServiceNode.getNodeInformation().getURL());
    }

    /**
    /**
     * Remove a no more waiting nodes accessor.
     * @param accessorToRemove the accessor to remove.
     */
    public void removeWaitingAccessor(P2PNodeLookup accessorToRemove) {
        this.waitingNodesLookup.remove(accessorToRemove);
        if (logger.isDebugEnabled()) {
            logger.debug("Accessor successfully removed");
        }
    }

    /**
     * @return the list of current acquaintances.
     */
    public Vector<P2PService> getAcquaintanceList() {
        return this.acquaintanceManager.getAcquaintanceList();
    }

    /**
     * Return  a stub of the local AcquaintanceManager active object
     * @return stub of local P2PAcquaintanceManager active object 
     */
    public P2PAcquaintanceManager getAcquaintanceManager() {
        return this.acquaintanceManager;
    }

    /**
     * Return  a stub of the P2PNodeManager active object
     * @return stub of local P2PNodeManager active object 
     */
    public P2PNodeManager getNodeManager() {
        return this.nodeManager;
    }

    /**
     * Remove a remote acquaintance
     * @param p remote acquaintance to remove.
     * @param acquaintancesURLs
     */
    public void remove(P2PService p, Vector<String> acquaintancesURLs) {
        this.acquaintanceManager.remove(p, acquaintancesURLs);
    }

    // -------------------------------------------------------------------------
    // Private class method
    // -------------------------------------------------------------------------

    /**
     * <b>* ONLY FOR INTERNAL USE *</b>
     * Generates a UUID and mark it as already received
     * @return a random UUID for sending message.
     */
    public UniversalUniqueID generateUuid() {
        while (oldMessageList.size() >= MSG_MEMORY) {
            oldMessageList.remove(0);
        }
        UniversalUniqueID uuid = UniversalUniqueID.randomUUID();
        oldMessageList.add(uuid);
        logger.debug(" UUID generated with #" + uuid);
        return uuid;
    }

    /**
     * Transmit this message on behalf of another local
     * active object (P2PAcquaintanceManager
     * Generates a UUID
     * @param m message to transmit
     * @param p target acquaintance
     */
    public void transmit(Message m, P2PService p) {
        m.setUuid(this.generateUuid());
        m.setSender(this.stubOnThis);
        if (logger.isDebugEnabled()) {
            logger.debug(" ----- Sender is " + m.getSender() + " by " + Thread.currentThread());
        }
        p.message(m);
    }

    /**
     * If not an old message and ttl > 1 return true else false.
     * @param ttl TTL of the message.
     * @param uuid UUID of the message.
     * @param remoteService P2PService of the first service.
     * @return true if you should broadcats, false else.
     */
    private boolean shouldTransmit(Message message) throws P2POldMessageException {
        int ttl = message.getTTL();
        UniversalUniqueID uuid = message.getUuid();

        // is it an old message?
        boolean isAnOldMessage = this.isAnOldMessage(uuid);

        //        String remoteNodeUrl = null;
        //        try {
        //            remoteNodeUrl = ProActive.getActiveObjectNodeUrl(remoteService);
        //        } catch (Exception e) {
        //            isAnOldMessage = true;
        //        }
        //String thisNodeUrl = this.p2pServiceNode.getNodeInformation().getURL();

        //        if (!isAnOldMessage && !remoteNodeUrl.equals(thisNodeUrl)) {
        if (!isAnOldMessage) {
            if (ttl > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Forwarding message request");
                }
                return message.shouldTransmit();
            }
            return false;
        }

        // it is an old message: nothing to do
        // NO REMOVE the isDebugEnabled message
        if (logger.isDebugEnabled()) {
            if (isAnOldMessage) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Old message request with #" + uuid);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("The peer is me: ");
                }
            }
        }

        throw new P2POldMessageException();
    }

    /**
     * If number of acquaintances is less than NOA return <code>true</code>, else
     * use random factor.
     * @param remoteService the remote service which is asking acquaintance.
     * @return <code>true</code> if this peer should be an acquaintance, else
     * <code>false</code>.
     */
    public boolean shouldBeAcquaintance(P2PService remoteService) {
        return this.acquaintanceManager.shouldBeAcquaintance(remoteService);
    }

    /**
     * If it's not an old message add the sequence number in the list.
     * @param uuid the uuid of the message.
     * @return <code>true</code> if it was an old message, <code>false</code> else.
     */
    private boolean isAnOldMessage(UniversalUniqueID uuid) {
        if (uuid == null) {
            return false;
        }
        if (oldMessageList.contains(uuid)) {
            return true;
        }
        if (oldMessageList.size() == MSG_MEMORY) {
            oldMessageList.remove(0);
        }
        oldMessageList.add(uuid);
        return false;
    }

    //--------------------------------------------------------------------------
    // Active Object methods
    //--------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entering initActivity");
        }
        this.service = new Service(body);

        try {
            // Reference to my current p2pServiceNode
            this.p2pServiceNode = NodeFactory.getNode(body.getNodeURL());
        } catch (NodeException e) {
            logger.fatal("Couldn't get reference to the local p2pServiceNode", e);
        }

        logger.info("P2P Service running in p2pServiceNode: " +
            this.p2pServiceNode.getNodeInformation().getURL());

        this.stubOnThis = (P2PService) PAActiveObject.getStubOnThis();

        Object[] params = new Object[2];
        params[0] = this.stubOnThis;
        params[1] = this.superPeers;
        try {
            // Active acquaintances
            this.acquaintanceManager = (P2PAcquaintanceManager) PAActiveObject.newActive(
                    P2PAcquaintanceManager.class.getName(), params, this.p2pServiceNode);

            if (logger.isDebugEnabled()) {
                logger.debug("P2P acquaintance manager activated");
            }

            // logger.debug("Got active group reference");

            // Active Node Manager
            this.nodeManager = (P2PNodeManager) PAActiveObject.newActive(P2PNodeManager.class.getName(),
                    null, this.p2pServiceNode);
            if (logger.isDebugEnabled()) {
                logger.debug("P2P node manager activated");
            }
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't create one of managers", e);
        } catch (NodeException e) {
            logger.fatal("Couldn't create one the managers", e);
        }
        logger.debug("Exiting initActivity");
    }

    /**
     * Return the local launched P2PService
     * @return P2PService of current runtime
     * @throws Exception
     */
    public static P2PService getLocalP2PService() throws Exception {
        UniversalBody body = ProActiveRuntimeImpl.getProActiveRuntime().getActiveObjects(P2P_NODE_NAME,
                P2PService.class.getName()).get(0);
        return (P2PService) MOP.newInstance(P2PService.class, (Object[]) null,
                Constants.DEFAULT_BODY_PROXY_CLASS_NAME, new Object[] { body });
    }

    /**
     * Ask to the Load Balancer object if the state is underloaded
     * @param ranking
     * @return <code>true</code> if the state is underloaded, <code>false</code> else.
     */
    public boolean amIUnderloaded(double ranking) {
        //        if (ranking >= 0) {
        //            return p2pLoadBalancer.AreYouUnderloaded(ranking);
        //        }
        //        return p2pLoadBalancer.AreYouUnderloaded();
        //TEST FAb
        return true;
    }

    /**
     * get an random Peer from current acquaintances.
     * @return a random reference to an alive peer. 
     * @throws PeerDoesntExist
     */
    public P2PService randomPeer() throws PeerDoesntExist {
        return this.acquaintanceManager.randomPeer();
    }
}
