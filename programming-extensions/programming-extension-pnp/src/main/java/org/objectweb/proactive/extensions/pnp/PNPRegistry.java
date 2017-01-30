/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.pnp;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.remoteobject.AlreadyBoundException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pnp.PNPConfig;


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
            logger.trace("Starting the registry for the PNP protocol");
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
    public void bind(String name, InternalRemoteRemoteObject body, boolean rebind) throws AlreadyBoundException {
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
