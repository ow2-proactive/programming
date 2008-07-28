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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class handles management of the ProActive group of acquaintances.
 * Because a ProActive group must not be shared between active objects (aiming to thread leak),
 * all actions concerning the group of acquaintances are performed by this object, and are executed exclusively 
 * by the activity thread of P2PAcquaintance Manager.<BR>
 * The group of acquaintances could have been wrapped in ProActive object,
 * and so the group could have been accessed by several active objects, but this kind of group wrapping doesn't permit
 * the caller to catch eventual ExceptionListException thrown by a dispatch or scatter call on the group. 
 *  
 * @author ProActive team
 *
 */
public class AcquaintancesWrapper implements Serializable {

    /**
     * Log4j logger name 
     */
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_ACQUAINTANCES);

    /**
     * ProActive group of acquaintances : a group containing
     * references to remote P2Service objects 
     */
    private P2PService acquaintances_active = null;

    /**
     * Group view of the acquaintances group.
     * This element must not be shared or thrown between active objects
     * because the group view is passed as deep copy. So add/remove actions
     * won't be consistent. All method calls and modifications on this group 
     * must be performed by this (object) and asked by P2PAcquaintanceManager.
     */
    private Group<P2PService> groupOfAcquaintances = null;

    /**
     * HashMap of P2PService and node's URL of the P2PService
     */
    private Map<P2PService, String> urlMap;

    /**
     * Constructor
     * Initialize the Active group of remote P2PService objects
     */
    public AcquaintancesWrapper() {

        urlMap = new HashMap<P2PService, String>();
        try {
            this.acquaintances_active = (P2PService) PAGroup.newGroup(P2PService.class.getName());
            this.groupOfAcquaintances = PAGroup.getGroup(acquaintances_active);
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return true if a remote P2PService is contained in the group.
     * @param p a P2PService object
     * @return true if the P2PService object is a member of the group, false otherwise.
     */
    public boolean contains(P2PService p) {
        return this.groupOfAcquaintances.contains(p);
    }

    /**
     * Add a remote P2PService to the group of acquaintances.
     * 
     * @param p stub of a remote P2Pservice to add  
     * @return true is the action has been performed successfully.
     */
    public boolean add(P2PService p) {
        boolean result = this.groupOfAcquaintances.add(p);

        if (result) {
            String peerUrl = PAActiveObject.getActiveObjectNodeUrl(p);
            urlMap.put(p, P2PService.getHostNameAndPortFromUrl(peerUrl));
            if (logger.isDebugEnabled()) {
                logger.debug("----- Adding " + peerUrl);
            }
        }
        return result;
    }

    /**
     * return stub of P2PService at rank i in the group
     * @param i Rank the P2PService asked
     * @return Stub of a remote P2PService
     */
    public P2PService get(int i) {
        return (P2PService) this.groupOfAcquaintances.get(i);
    }

    /**
     * Return the group of acquaintances
     * @return P2Pservice object representing the group
     */
    public P2PService getAcquaintancesGroup() {
        return this.acquaintances_active;
    }

    /**
     * Return in a Vector all acquaintances of the group
     * @return a Vector containing the acquaintances
     */
    public Vector<P2PService> getAcquaintancesAsList() {
        return new Vector<P2PService>(groupOfAcquaintances);
    }

    /**
     * Get the group's size
     * @return an integer representing the group's size.
     */
    public int size() {
        return this.groupOfAcquaintances.size();
    }

    /** Return a String array of all acquaintances URLs
     * stored in the group 
     * @return A string a array of current acquaintances URLS
     */
    public String[] getAcquaintancesURLs() {
        return urlMap.values().toArray(new String[] {});
    }

    /**
     * Remote the remote Peer to peer service from the group
     * and from the hashMap P2P2Service-P2PUrl
     * @param remoteService remote P2PSerivce object to remove
     * @return true if the removal has been successful, false otherwise
     */
    public boolean remove(P2PService remoteService) {
        boolean result = this.groupOfAcquaintances.remove(remoteService);
        if (result) {
            String url = this.urlMap.get(remoteService);
            this.urlMap.remove(remoteService);
            logger.info("------ Removing " + url);
        }
        return result;
    }
}
