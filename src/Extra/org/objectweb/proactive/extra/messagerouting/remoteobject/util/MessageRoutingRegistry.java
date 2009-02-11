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
package org.objectweb.proactive.extra.messagerouting.remoteobject.util;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** A registry for the message routing protocol
 * 
 * This registry is currently implemented as a singleton because lookup message
 * must be able to find it. If the RemoteObjectFactory can easily be reached by
 * the message, then the singleton can be removed.
 * 
 * @since ProActive 4.1.0
 */
public class MessageRoutingRegistry {
    static final Logger logger = ProActiveLogger.getLogger(Loggers.FORWARDING_REMOTE_OBJECT);

    public final static MessageRoutingRegistry singleton = new MessageRoutingRegistry();

    /** Registered Remote Objects */
    private Map<URI, InternalRemoteRemoteObject> rRemteObjectMap;

    private MessageRoutingRegistry() {
        if (logger.isTraceEnabled()) {
            logger.trace("Starting the registry for the message routing protocol");
        }
        this.rRemteObjectMap = Collections.synchronizedMap(new HashMap<URI, InternalRemoteRemoteObject>());
    }

    /**
     * Binds a RemoteObject with an URI
     * 
     * @param uri
     *            the uri of the remote object
     * @param body
     *            the remote object
     */
    public void bind(URI uri, InternalRemoteRemoteObject body) {
        rRemteObjectMap.put(uri, body);
        if (logger.isDebugEnabled()) {
            logger.debug("Added " + uri + " into the registry");
        }
    }

    /**
     * Unbinds a remote object from an URI
     * 
     * @param uri
     *            the uri of the remote object
     */
    public void unbind(URI uri) {
        rRemteObjectMap.remove(uri);
        if (logger.isDebugEnabled()) {
            logger.debug("Removed " + uri + " from the registry");
        }

    }

    /** Gives all the URIs registered in this registry */
    public URI[] list() {
        URI[] list = new URI[rRemteObjectMap.size()];
        rRemteObjectMap.keySet().toArray(list);
        return list;
    }

    /**
     * Retrieves a remote object from an URI
     * 
     * @param uri
     *            The URI of the remote object to be retrieved
     * @return the binded remote object
     */
    public InternalRemoteRemoteObject lookup(URI uri) {
        return rRemteObjectMap.get(uri);
    }
}
