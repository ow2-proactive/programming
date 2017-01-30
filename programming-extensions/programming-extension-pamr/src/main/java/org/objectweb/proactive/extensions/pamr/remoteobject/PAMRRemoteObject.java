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
package org.objectweb.proactive.extensions.pamr.remoteobject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.client.Agent;
import org.objectweb.proactive.extensions.pamr.remoteobject.message.PAMRRemoteObjectRequest;


/**
 * 
 * @since ProActive 4.1.0
 */

public class PAMRRemoteObject implements RemoteRemoteObject, Serializable {
    final static private Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_REMOTE_OBJECT);

    /** The URL of the RemoteObject */
    private URI remoteObjectURL;

    /** The local message routing agent 
     *
     * This field must NOT be used since it is set by the getAgent() method. Each time this
     * object is sent on a remote runtime, the local agent needs to be retrieved. Custom readObject()
     * is avoid by the use of a transient field and the getAgent() method.
     */
    private transient Agent cachedAgent;

    protected transient InternalRemoteRemoteObject remoteObject;

    public PAMRRemoteObject(InternalRemoteRemoteObject remoteObject, URI remoteObjectURL, Agent agent) {
        this.remoteObject = remoteObject;
        this.remoteObjectURL = remoteObjectURL;
        this.cachedAgent = agent;
    }

    public Reply receiveMessage(Request message) throws IOException {
        Agent agent = getAgent();
        if (agent == null) {
            throw new IOException("Failed to retrieve local PAMR agent (bad configuration ?)");
        }

        PAMRRemoteObjectRequest req = new PAMRRemoteObjectRequest(message, this.remoteObjectURL, agent);
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

    private Agent getAgent() {
        if (this.cachedAgent == null) {
            try {
                // FIXME: The factory cast is a hack but there is no clean way to do it
                PAMRRemoteObjectFactory f;
                f = (PAMRRemoteObjectFactory) AbstractRemoteObjectFactory.getRemoteObjectFactory("pamr");
                this.cachedAgent = f.getAgent();
            } catch (UnknownProtocolException e) {
                logger.fatal("Failed to get the local message routing agent", e);
            }
        }
        return this.cachedAgent;
    }

}
