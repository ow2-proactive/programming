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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.group.ExceptionInGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.extra.p2p.service.exception.PeerDoesntExist;
import org.objectweb.proactive.extra.p2p.service.messages.AcquaintanceReply;
import org.objectweb.proactive.extra.p2p.service.messages.AcquaintanceRequest;
import org.objectweb.proactive.extra.p2p.service.messages.Message;
import org.objectweb.proactive.extra.p2p.service.util.P2PConstants;


/**
 * This active object manage the list of neighbors peers, i.e. known and active peers,
 * called acquaintances. Acquaintances are stored in a ProActive Group. This active object probes
 * periodically its acquaintances and tries to lookup new ones, and so maintains a list of alive acquaintances,
 *  conform to its NOA number. This object is responsible of dispatching P2P messages,
 * through the peer to peer infrastructure.
 * 
 * @author ProActive team
 * 
 */
public class P2PAcquaintanceManager implements InitActive, RunActive, Serializable, P2PConstants,
        ProActiveInternalObject {

    /**
     * The maximum waiting time before considering an ACQ request is lost and
     * should be resent
     */
    private static long MAX_WAIT_TIME = 10000;
    /**
     * P2P logger 
     */
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_ACQUAINTANCES);

    /**
     * Max number of acquaintance that the P2PAcquaintanceManager has to know 
     */
    static public int NOA = Integer.parseInt(PAProperties.PA_P2P_NOA.getValue());

    /**
     * generator of random values
     */
    private Random randomizer = new Random();

    /**
     * A ProActive Stub on the local P2PService
     */
    private P2PService localService = null;

    /**
     * ProActive group of remote acquaintances 
     */
    private P2PService acquaintancesGroup = null;

    /**
     * Wrapper of the ProActive group of acquaintances, 
     * used to centralize in one point all group modification (add, remove
     * members in the group...)  
     */
    protected AcquaintancesWrapper acquaintancesWrapper;

    /**
     * Store the name of awaited replies for setting acquaintances 
     */
    protected HashMap<String, DatedRequest> awaitedReplies = new HashMap<String, DatedRequest>();

    /**
     * list of favorite acquaintances
     */
    private HashSet<String> preferedAcquaintancesURLs = new HashSet<String>();

    /**
     * The ProActive empty constructor for activating.
     */
    public P2PAcquaintanceManager() {

    }

    /**
     * Construct a new <code>P2PAcquaintanceManager</code>.
     * 
     * @param localService
     *            a reference to the local P2P service.
     * @param prefferedAcq
     *            a list of first-contact acquaintances
     */
    public P2PAcquaintanceManager(P2PService localService, Vector<String> prefferedAcq) {
        this.localService = localService;
        setPreferedAcq(prefferedAcq);
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        this.acquaintancesWrapper = new AcquaintancesWrapper();
        this.acquaintancesGroup = acquaintancesWrapper.getAcquaintancesGroup();
        logger.debug("Group of exportAcquaintances successfuly created");
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        body.getRequestQueue();
        while (body.isActive()) {
            if (this.acquaintancesWrapper.size() > 0) {

                logger.debug("Sending heart-beat");
                try {
                    this.acquaintancesGroup.heartBeat();
                } catch (ExceptionListException e) {
                    tidyUpGroup(e);
                }
                logger.debug("Heart-beat sent");
            }

            if (this.getEstimatedNumberOfAcquaintances() < NOA) {
                lookForNewPeers();
            } else if (this.acquaintancesWrapper.size() > NOA) {
                // we should drop some here
                // do we go for all at once or just one at a time?
                logger.info("I have too many neighbors!");
                this.dropRandomPeer();
            } else {
                if (logger.isDebugEnabled()) {
                    if (this.getEstimatedNumberOfAcquaintances() >= NOA)
                        logger.debug("I have reached the maximum of acquaintance ");
                }
            }
            waitTTU(service);
            // this.dumpTables();
            this.cleanAwaitedReplies();
        }
    }

    private void tidyUpGroup(ExceptionListException e) {
        Iterator<ExceptionInGroup> it = e.iterator();
        while (it.hasNext()) {
            ExceptionInGroup exceptionInGroup = (ExceptionInGroup) it.next();
            this.acquaintancesWrapper.remove((P2PService) exceptionInGroup.getObject());
        }
    }

    /**
     * Try to find new peers in the peer to peer infrastructure.
     * First try to get peer from list of favorite peers
     * After launch exploration message to get new peers if needed
     * to move on these two actions
     * 
     */
    protected void lookForNewPeers() {
        // How many peers ?
        if (this.getEstimatedNumberOfAcquaintances() < NOA) {
            // Looking for new peers
            logger.debug("NOA is " + NOA + " - Size of P2PAcquaintanceManager is " +
                this.getEstimatedNumberOfAcquaintances() +
                " looking for new acquaintances through prefered ones");
            this.connectToPreferedAcquaintances();
        }

        // How many peers ?
        if (this.getEstimatedNumberOfAcquaintances() < NOA) {
            // Looking for new peers
            logger.debug("NOA is " + NOA + " - Size of P2PAcquaintanceManager is " +
                this.getEstimatedNumberOfAcquaintances() +
                " looking for new acquaintances through exploration");

            this.localService.explore();
            logger.debug("Explorating message sent");
        }
    }

    /**
     * Serve requests during the TTU time. 
     * @param service
     */
    protected void waitTTU(Service service) {
        // Waiting TTU & serving requests
        logger.debug("Waiting for " + P2PService.TTU + "ms");
        long endTime = System.currentTimeMillis() + P2PService.TTU;
        service.blockingServeOldest(P2PService.TTU);
        while (System.currentTimeMillis() < endTime) {
            try {
                service.blockingServeOldest(endTime - System.currentTimeMillis());
            } catch (ProActiveRuntimeException e) {
                e.printStackTrace();
                logger.debug("Certainly because the body is not active", e);
            }
        }
        logger.debug("End waiting");
    }

    /**
     * try to get acquaintances from the list of favorite acquaintances 
     */
    public void connectToPreferedAcquaintances() {

        HashSet<String> newSet = new HashSet<String>();
        String tmp = null;
        Iterator<String> it = this.preferedAcquaintancesURLs.iterator();
        while (it.hasNext() && (this.getEstimatedNumberOfAcquaintances() < NOA)) {

            tmp = (String) it.next();
            it.remove();
            String peerUrl = buildCorrectUrl(urlAdderP2PNodeName(tmp));

            try {
                Node distNode = NodeFactory.getNode(peerUrl);
                P2PService peer = (P2PService) distNode.getActiveObjects(P2PService.class.getName())[0];

                //if the remote peer is not contained in the current group
                //of acquaintances, try the handshake with it
                if (!this.contains(peer).booleanValue()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("P2PAcquaintanceManager requesting peer " + peerUrl);
                    }
                    // Send a message to the remote peer to register myself
                    startAcquaintanceHandShake(peerUrl, peer);
                } else {
                    newSet.add(peerUrl);
                }
            } catch (NodeException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The peer at " + peerUrl + " couldn't be contacted");
                }
                // put it back for later use
                newSet.add(peerUrl);
            } catch (ActiveObjectCreationException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The peer at " + peerUrl + " couldn't be contacted");
                }
                // put it back for later use
                newSet.add(peerUrl);
            }
        }

        if (this.size().intValue() == 0) {
            logger.info("No peer could be found to join the network");
        } else {
            newSet.addAll(this.preferedAcquaintancesURLs);
        }
        this.preferedAcquaintancesURLs = newSet;
    }

    /**
     * Ask to the Acquaintance group to send a message
     * @param mess
     */
    public void dispatchMessage(Message mess) {
        this.acquaintancesGroup.message(mess);
    }

    /**
     * Remove awaited requests which have timeouted
     */
    @SuppressWarnings("unchecked")
    public void cleanAwaitedReplies() {
        // System.out.println("P2PAcquaintanceManager.cleanAwaitedReplies()
        // still " + awaitedReplies.size() );
        ArrayList<String> urls = new ArrayList<String>();
        Set<Map.Entry<String, DatedRequest>> map = awaitedReplies.entrySet();
        Iterator it = map.iterator();
        while (it.hasNext()) {
            Map.Entry<String, DatedRequest> entry = (Map.Entry<String, DatedRequest>) it.next();

            if ((System.currentTimeMillis() - (entry.getValue()).getTime()) > MAX_WAIT_TIME) {
                if (logger.isDebugEnabled()) {
                    logger.debug("xxxxx Peer " + entry.getKey() + " did not reply to our request");
                }
                // this guy did not reply so we should put it back in the
                // preferedACQList
                urls.add(entry.getKey());
                it.remove();
            }
        }
        it = urls.iterator();
        while (it.hasNext()) {
            this.preferedAcquaintancesURLs.add((String) it.next());
        }
    }

    /**
     * Starts an acquaintance handshake. Send a message to the peer and add it to
     * the awaited replies list
     * 
     * @param peerUrl
     * @param peer
     */
    public void startAcquaintanceHandShake(String peerUrl, P2PService peer) {
        this.localService.transmit(new AcquaintanceRequest(1), peer);

        if (logger.isDebugEnabled()) {
            logger.debug("XXXXXX putting in awaited List " + peerUrl);
        }
        awaitedReplies.put(buildCorrectUrl(peerUrl), new DatedRequest(peer, System.currentTimeMillis()));
    }

    /**
     * Add a peer in the group of acquaintances Add only if not already present
     * and still some space left (NOA)
     * 
     * @param peer
     *            the peer to add.
     * @return add succesfull
     */
    public Vector<String> add(P2PService peer) {
        return this.add(buildCorrectUrl(PAActiveObject.getActiveObjectNodeUrl(peer)), peer);
    }

    /**
     * Add a peer in the group of acquaintances Add only if not already present
     * and still some space left (NOA)
     * 
     * @param peerUrl
     *            the url of the peer
     * @param peer
     *            the peer to add
     * @return add succesfull
     */
    public Vector<String> add(String peerUrl, P2PService peer) {
        boolean result = false;
        try {
            if (this.shouldBeAcquaintance(peer)) {
                if (!peerUrl.matches(".*cannot contact the body.*")) {
                    result = this.acquaintancesWrapper.add(peer);
                    logger.info("Acquaintance " + peerUrl + " " + result + " added");
                }
                return null;
            }
        } catch (Exception e) {
            this.acquaintancesWrapper.remove(peer);
            if (logger.isDebugEnabled()) {
                logger.debug("Problem when adding peer", e);
            }
        }
        return this.getAcquaintancesURLs();
    }

    /**
     * Method called by an {@link AcquaintanceReply} message, in order to accept this P2PService as a new acquaintance.
     * @param url URL of the remote peer
     * @param peer stub of a remote P2PService
     */
    public void acqAccepted(String url, P2PService peer) {
        if (logger.isDebugEnabled()) {
            logger.debug("P2PAcquaintanceManager.acqAccepted() got a reply from " + url);
            logger.debug("URL:" + url + " PEER:" + peer);
        }

        // we remove it from the awaited answers
        // if we don't do so, it might be refused because of the NOA limit
        this.removeFromAwaited(url);
        this.add(url, peer);
        if (logger.isDebugEnabled()) {
            logger.debug("P2PAcquaintanceManager.acqAccepted() adding " + "--" + url + "--");
        }
        this.preferedAcquaintancesURLs.add(url);
        if (logger.isDebugEnabled()) {
            Iterator<String> it = this.preferedAcquaintancesURLs.iterator();
            while (it.hasNext()) {
                logger.debug("            " + it.next());
            }
        }
    }

    /**
     * Method called by an {@link AcquaintanceReply} message, 
     * in order to refuse this P2PService as a new acquaintance.
     * @param url
     * @param s
     */
    public void acqRejected(String url, Vector<String> s) {
        if (logger.isDebugEnabled()) {
            logger.debug("P2PAcquaintanceManager.acqRejected() " + url);
        }
        this.removeFromAwaited(url);
        this.addToPreferedAcq(s);
        // we add it back
        this.preferedAcquaintancesURLs.add(url);
    }

    /**
     * Remove a peer URL from the list of awaited replies
     * @param url
     */
    public void removeFromAwaited(String url) {
        if (logger.isDebugEnabled()) {
            String[] tmp = this.getAwaitedRepliesUrls();
            for (int i = 0; i < tmp.length; i++) {
                logger.debug("--" + tmp[i] + "--");
            }
        }

        awaitedReplies.remove(url);
        if (logger.isDebugEnabled()) {
            logger.debug("Removing --" + url + "-- from awaited peers ");
        }
    }

    /**
     * Remove the peer from the current acquaintances. Add the acquaintancesURLs
     * to the favorite acquaintances
     * 
     * @param peer
     * @param acquaintancesURLs
     */
    public void remove(P2PService peer, Vector<String> acquaintancesURLs) {
        boolean result = this.acquaintancesWrapper.remove(peer);
        if (acquaintancesURLs != null) {
            this.addToPreferedAcq(acquaintancesURLs);
        }
        if (result) {
            logger.debug("Peer successfully removed");
        } else {
            logger.debug("Peer not removed");
        }
    }

    /**
     * Remove a random peer picked from acquaintances group.
     * Remove a random peer from the group of current
     * acquaintances, and ask to this peer to be removed
     * from its c 
     */
    protected void dropRandomPeer() {
        try {
            // pick up a random peer in the list
            P2PService p = randomPeer();
            // logger.info(" I have decided to drop " + p.getAddress());
            this.remove(p, null);
            p.remove(this.localService, this.getAcquaintancesURLs());
        } catch (PeerDoesntExist e) {

        }
    }

    /**
     * Get in a vector the list of current acquaintances.
     * @return a vector containing URLS of current acquaintances
     */
    public Vector<String> getAcquaintancesURLs() {
        return new Vector<String>(Arrays.asList(this.acquaintancesWrapper.getAcquaintancesURLs()));
    }

    /**
     * Returns the number of elements in the group of acquaintances.
     * @return the number of elements in this group.
     */
    public IntMutableWrapper size() {
        return new IntMutableWrapper(this.acquaintancesWrapper.size());
    }

    /**
     * Return an estimation of futur number of acquaintances know
     * This estimation is calculated with the current number acquaintances,
     * added to the number of awaited responses of Acquaintance handshake
     * messages sent.
     * @return number of estimated acquaintances
     */
    public int getEstimatedNumberOfAcquaintances() {
        return this.acquaintancesWrapper.size() + awaitedReplies.size();
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified
     * element. More formally, returns <tt>true</tt> if and only if this
     * collection contains at least one element <tt>e</tt> such that
     * <tt>(o==null ? e==null : o.equals(e))</tt>.
     * 
     * @param service
     *            element whose presence in this collection is to be tested.
     * @return <tt>true</tt> if this collection contains the specified
     *         element.
     */
    public BooleanMutableWrapper contains(P2PService service) {
        return new BooleanMutableWrapper(this.acquaintancesWrapper.contains(service));
    }

    /**
     * @return a random acquaintance reference.
     * @throws PeerDoesntExist
     */
    // TODO: unsafe code
    public P2PService randomPeer() throws PeerDoesntExist {
        if (this.acquaintancesWrapper.size() > 0) {
            int random = this.randomizer.nextInt(this.acquaintancesWrapper.size());
            return this.acquaintancesWrapper.get(random);
        } else
            throw new PeerDoesntExist();
    }

    /**
     * Return a vector containing all current acquaintances
     * @return the list of current acquaintances.
     */
    public Vector<P2PService> getAcquaintanceList() {
        return this.acquaintancesWrapper.getAcquaintancesAsList();
    }

    /**
     * Return the NOA
     * @return an integer representing the NOA
     */
    public int getMaxNOA() {
        return NOA;
    }

    /**
     * Return true is a remote P2PService should be registered as a new
     * acquaintance.
     * @param remoteService
     * @return true if the remote peer is know registered as a
     */
    public boolean shouldBeAcquaintance(P2PService remoteService) {
        if (this.contains(remoteService).booleanValue()) {
            if (logger.isDebugEnabled()) {
                logger.debug("The remote peer is already known");
            }
            return false;
        }
        return acceptAnotherAcquaintance();
    }

    /**
     * Indicates whether or not a new acquaintance should be accepted This is
     * defined using a probability Always accept if 0 <=
     * getEstimatedNumberOfAcquaintances() < NOA Accept with probability P if
     * NOA <= getEstimatedNumberOfAcquaintances() < 2*NOA Reject otherwise The
     * probability is linear y = -1/NOA*estimatedNumberOfAcquaintances + 2
     * 
     * @return a boolean
     */
    protected boolean acceptAnotherAcquaintance() {
        if (this.getEstimatedNumberOfAcquaintances() < NOA) {
            if (logger.isDebugEnabled()) {
                logger.debug("NOA not reached: I should be an acquaintance");
            }
            return true;
        }
        if (this.getEstimatedNumberOfAcquaintances() > (2 * NOA)) {
            if (logger.isDebugEnabled()) {
                logger.debug("2*NOA reached, I refuse the acquaintance");
            }
            return false;
        }
        // we are in the grey area, only accept with some probability
        // first compute the probability according to the max number
        double prob = (-(1.0 / this.getMaxNOA()) * this.getEstimatedNumberOfAcquaintances()) + 2;
        if (logger.isDebugEnabled()) {
            logger.debug("estimatedNOA " + this.getEstimatedNumberOfAcquaintances());
            logger.debug("Probability to accept set to " + prob);
        }
        return (randomizer.nextDouble() <= prob);
    }

    /**
     * Set with a vector of peer URLs,
     * the list of favorite acquaintances
     * @param v the vector to add
     */
    public void setPreferedAcq(Vector<String> v) {
        if (logger.isDebugEnabled()) {
            logger.debug("SET PREFFERED ACQUAINTANCE LIST");
        }
        this.preferedAcquaintancesURLs = new HashSet<String>();
        Iterator<String> it = v.iterator();
        while (it.hasNext()) {
            String p = buildCorrectUrl(it.next());
            if (logger.isDebugEnabled()) {
                logger.debug(p);
            }
            this.preferedAcquaintancesURLs.add(p);
        }
    }

    /**
     * Add the given peer URLs to the current favorite acquaintances list
     * 
     * @param v
     *            the list of acquaintances
     */
    public void addToPreferedAcq(Vector<String> v) {
        this.preferedAcquaintancesURLs.addAll(v);
    }

    /** Return of remote peer URLs that acquaintanceManager wait for replies. 
     * @return a string array of peers URL of, that P2Pacquaintance wait for reply  
     */
    public String[] getAwaitedRepliesUrls() {
        return this.awaitedReplies.keySet().toArray(new String[] {});
    }

    /**
     * Add the default name of the P2P Node to a specified <code>URL</code>.
     * @param url the URL.
     * @return the <code>URL</code> with the name of the P2P Node.
     */
    private static String urlAdderP2PNodeName(String url) {
        if (url.indexOf(P2P_NODE_NAME) <= 0) {
            url += ("/" + P2P_NODE_NAME);
        }
        return url;
    }

    /**
     * Add rmi:// in front of all URLS
     * 
     * @param s
     * @return
     */
    private String buildCorrectUrl(String s) {
        if (s.indexOf("//") < 0) {
            s = "//" + s;
        }
        if (s.indexOf("rmi:") < 0) {
            s = "rmi:" + s;
        }
        if (s.indexOf(P2PConstants.P2P_NODE_NAME) < 0) {
            s = s + "/" + P2PConstants.P2P_NODE_NAME;
        }
        return s;
    }

    /**
     * Set the current value of NOA 
     * @param noa the new value of NOA to set
     */
    public void setMaxNOA(int noa) {
        logger.info("P2PAcquaintanceManager.setNOA() changing noa from " + NOA + " to " + noa);
        P2PAcquaintanceManager.NOA = noa;
    }

    /**
     * Print in the console the list of favorite acquaintances
     * and the list of awaited replies.
     */
    public void dumpTables() {
        System.out.println("----- Prefered Acquaintances ---");
        Iterator<String> it = preferedAcquaintancesURLs.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }

        System.out.println("---------------------------------------------");
        System.out.println("----- Awaited Replies ---" + this.awaitedReplies.size());
        Set<Map.Entry<String, DatedRequest>> map = awaitedReplies.entrySet();
        Iterator<Map.Entry<String, DatedRequest>> it2 = map.iterator();
        while (it2.hasNext()) {
            Map.Entry<String, DatedRequest> entry = it2.next();

            System.out.println(entry.getKey() + " requested at " + (entry.getValue()).getTime());
        }

        System.out.println("---------------------------------------------");
    }

    /**
     * Inner A class to remember when an ACQ request has been issued
     * 
     * 
     */
    private class DatedRequest {
        protected P2PService service;
        protected long time;

        DatedRequest(P2PService s, long t) {
            this.service = s;
            this.time = t;
        }

        /** Return the timeStamp of the datedRequest
         * @return timeStamp
         */
        public long getTime() {
            return this.time;
        }

        /**
         * Return the remote P2PService associated to the request 
         * @return remote P2PService associated to the request
         */
        public P2PService getP2PService() {
            return service;
        }
    }
}
