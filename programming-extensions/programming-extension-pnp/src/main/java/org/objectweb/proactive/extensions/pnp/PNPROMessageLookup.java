/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pnp;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** Represents a lookup message
 *
 * When processed, this message performs return the RemoteObject associated to
 * the given URI.
 *
 * @since ProActive 4.3.0
 */

class PNPROMessageLookup extends PNPROMessage implements Serializable {

    private static final long serialVersionUID = 62L;
    final String name;

    /**
     * Construct a lookup message
     *
     * @param uri
     *            The URI of the RemoteObject to be retrieved
     * @param agent
     *            The local agent to use to send the message
     */

    public PNPROMessageLookup(URI uri, String name, PNPAgent agent) {
        super(uri, agent);
        this.name = name;
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
            InternalRemoteRemoteObject irro = PNPRegistry.singleton.lookup(name);
            if (irro != null) {
                RemoteRemoteObject rro = null;
                try {
                    PNPRemoteObjectFactoryAbstract f = (PNPRemoteObjectFactoryAbstract) AbstractRemoteObjectFactory
                            .getRemoteObjectFactory(this.uri.getScheme());
                    rro = f.newRemoteObject(irro);
                    ((PNPRemoteObject) rro).setURI(uri);
                    return rro;
                } catch (UnknownProtocolException e) {
                    // Impossible because that class has been created by the factory
                    ProActiveLogger.logImpossibleException(logger, e);
                } catch (ProActiveException e) {
                    // newRemoteObject failed (rof failed to initialize ?)
                    logger.info("PNP failed to create the remote object after lookup: " + uri, e);
                    return null;
                }
            } else {
                logger.info("Someone performed a lookup on " + uri + " but this remote object is not known");
            }
        } else {
            logger
                    .warn("Tried to perform a lookup on null. This is probably a bug in the PNPRemoteObjectFactory");
        }

        return null;
    }
}
