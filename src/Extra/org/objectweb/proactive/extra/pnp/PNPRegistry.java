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
package org.objectweb.proactive.extra.pnp;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.remoteobject.AlreadyBoundException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.pnp.PNPConfig;


/** A registry for the PNP protocol
 *
 * This registry is currently implemented as a singleton because lookup message
 * must be able to find it. If the RemoteObjectFactory can easily be reached by
 * the message, then the singleton can be removed.
 *
 * @since ProActive 4.3.0
 */
class PNPRegistry {
    static final Logger logger = ProActiveLogger.getLogger(PNPConfig.Loggers.PNP);

    public final static PNPRegistry singleton = new PNPRegistry();

    /** Registered Remote Objects */
    private ConcurrentHashMap<String, InternalRemoteRemoteObject> rRemteObjectMap;

    private PNPRegistry() {
        if (logger.isTraceEnabled()) {
            logger.trace("Starting the registry for the message routing protocol");
        }
        this.rRemteObjectMap = new ConcurrentHashMap<String, InternalRemoteRemoteObject>();
    }

    /**
     * Binds a RemoteObject with an URI
     *
     * @param uri
     *            the uri of the remote object
     * @param body
     *            the remote object
     */
    public void bind(String name, InternalRemoteRemoteObject body, boolean rebind)
            throws AlreadyBoundException {
        if (rebind) {
            rRemteObjectMap.put(name, body);
        } else {
            InternalRemoteRemoteObject r = rRemteObjectMap.putIfAbsent(name, body);
            if (r != null) {
                throw new AlreadyBoundException("A remote object is already bound to " + name);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Added " + name + " into the registry");
        }
    }

    /**
     * Unbinds a remote object from an URI
     *
     * @param uri
     *            the uri of the remote object
     */
    public void unbind(String name) {
        rRemteObjectMap.remove(name);
        if (logger.isDebugEnabled()) {
            logger.debug("Removed " + name + " from the registry");
        }

    }

    /** Gives all the URIs registered in this registry */
    public String[] list() {
        String[] list = new String[rRemteObjectMap.size()];
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
    public InternalRemoteRemoteObject lookup(String name) {
        return rRemteObjectMap.get(name);
    }
}
