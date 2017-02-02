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
package org.objectweb.proactive.extensions.pamr.remoteobject.message;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.client.Agent;
import org.objectweb.proactive.extensions.pamr.remoteobject.PAMRRemoteObject;
import org.objectweb.proactive.extensions.pamr.remoteobject.PAMRRemoteObjectFactory;
import org.objectweb.proactive.extensions.pamr.remoteobject.util.PAMRRegistry;


/** Represents a lookup message
 * 
 * When processed, this message performs return the RemoteObject associated to
 * the given URI.
 * 
 * @since ProActive 4.1.0
 */

public class PAMRRemoteObjectLookupMessage extends PAMRMessage implements Serializable {

    /**
     * Construct a lookup message
     * 
     * @param uri
     *            The URI of the RemoteObject to be retrieved
     * @param agent
     *            The local agent to use to send the message
     */

    public PAMRRemoteObjectLookupMessage(URI uri, Agent agent) {
        super(uri, agent);
    }

    /** Get the remote object */
    // client side
    public RemoteRemoteObject getReturnedObject() {
        return (RemoteRemoteObject) this.returnedObject;
    }

    @Override
    // server side
    public Object processMessage() {
        if (logger.isTraceEnabled()) {
            logger.trace("Executing a lookup message for " + uri);
        }

        if (this.uri != null) {
            InternalRemoteRemoteObject irro = PAMRRegistry.singleton.lookup(uri);
            if (irro != null) {
                RemoteRemoteObject rro = null;
                try {
                    PAMRRemoteObjectFactory f = (PAMRRemoteObjectFactory) AbstractRemoteObjectFactory.getRemoteObjectFactory(PAMRRemoteObjectFactory.PROTOCOL_ID);
                    rro = f.newRemoteObject(irro);
                    ((PAMRRemoteObject) rro).setURI(uri);
                    return rro;
                } catch (ProActiveException e) {
                    // Impossible because that class has been created by the factory
                    ProActiveLogger.logImpossibleException(logger, e);
                }
            } else {
                logger.info("Someone performed a lookup on " + uri + " but this remote object is not known");
            }
        } else {
            logger.warn("Tried to perform a lookup on null. This is probably a bug in the MessageRoutingRemoteObjectFactory");
        }

        return null;
    }
}
