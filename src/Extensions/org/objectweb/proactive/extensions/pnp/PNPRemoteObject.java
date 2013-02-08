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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;


/**
 *
 * @since ProActive 4.3.0
 */
class PNPRemoteObject implements RemoteRemoteObject, Serializable {
    final static private Logger logger = ProActiveLogger.getLogger(PNPConfig.Loggers.PNP);

    /** The URL of the RemoteObject */
    private URI remoteObjectURL;

    /** The local message routing agent
     *
     * This field must NOT be used since it is set by the getAgent() method. Each time this
     * object is sent on a remote runtime, the local agent needs to be retrieved. Custom readObject()
     * is avoid by the use of a transient field and the getAgent() method.
     */
    private transient PNPAgent agent;

    protected transient InternalRemoteRemoteObject remoteObject;

    public PNPRemoteObject(InternalRemoteRemoteObject remoteObject, URI remoteObjectURL, PNPAgent agent) {
        this.remoteObject = remoteObject;
        this.remoteObjectURL = remoteObjectURL;
        this.agent = agent;
    }

    public Reply receiveMessage(Request message) throws IOException {
        PNPROMessageRequest req = new PNPROMessageRequest(message, this.remoteObjectURL, getAgent());
        req.send();
        SynchronousReplyImpl rep = (SynchronousReplyImpl) req.getReturnedObject();
        return rep;
    }

    public void setURI(URI url) {
        this.remoteObjectURL = url;
    }

    public URI getURI() {
        return this.remoteObjectURL;
    }

    private PNPAgent getAgent() {
        if (this.agent == null) {
            try {
                // FIXME: The factory cast is a hack but there is no clean way to do it
                PNPRemoteObjectFactoryAbstract f;
                f = (PNPRemoteObjectFactoryAbstract) AbstractRemoteObjectFactory
                        .getRemoteObjectFactory(this.remoteObjectURL.getScheme());
                this.agent = f.getAgent();
            } catch (UnknownProtocolException e) {
                logger.fatal("Failed to get the local message routing agent", e);
            }
        }
        return this.agent;
    }

}
