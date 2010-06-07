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
package org.objectweb.proactive.extensions.pamr.remoteobject.message;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.converter.remote.ProActiveMarshaller;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.client.Agent;
import org.objectweb.proactive.extensions.pamr.exceptions.MessageRoutingException;


/** Any kind of routed message.
 * 
 * @since ProActive 4.1.0 
 */

public abstract class MessageRoutingMessage implements Serializable {
    static final Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.FORWARDING_REMOTE_OBJECT);

    /** The recipient of this message */
    final protected URI uri;

    /** The local agent to use to send the message 
     *
     * Once the message has been sent, the agent is never used again. So this field can
     * safely be transient. Anyway, an external entity should be in charge of message sending
     * for a given runtime instead of embedding the logic in each message.
     */
    transient final protected Agent agent;

    /** The response to this message 
     *
     * <b>warning</b> send() and getReturnedObject() are <b>NOT thread safe</b>.
     * send will block until the response is received but getReturnedObject() is only 
     * a getter. It will return null is the response has not yet been received.
     */
    protected Object returnedObject;

    /** serialization
     *  This field is transient - it has significance only on this host
     * */
    private transient final ProActiveMarshaller marshaller;

    protected boolean isAsynchronous = false;

    public MessageRoutingMessage(URI uri, Agent agent) {
        this.uri = uri;
        this.agent = agent;
        this.returnedObject = null;
        // get the runtime URL
        // TODO - maybe properly synchronize, to make sure that
        // MessageRoutingROF.createRemoteObject() for a PART was called before
        String runtimeUrl = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
        this.marshaller = new ProActiveMarshaller(runtimeUrl);
    }

    /**
     * Processes the message.
     * @return an object as a result of the execution of the message
     */
    public abstract Object processMessage();

    /** Send the message to its recipient using the local agent
     * 
     * @throws MessageRoutingException if something bad happened when sending this message
     */
    public final void send() throws MessageRoutingException {
        try {
            byte[] bytes = this.marshaller.marshallObject(this);
            byte[] response = agent.sendMsg(this.uri, bytes, isAsynchronous);
            if (!isAsynchronous) {
                this.returnedObject = this.marshaller.unmarshallObject(response);
            }
        } catch (MessageRoutingException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to send message to " + this.uri, e);
            }
            throw e;
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.error("Failed to serialize this message, reason:" + e.getMessage(), e);
            }
            throw new MessageRoutingException(e);
        } catch (ClassNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.error("Failed to deserialize the reply for this message, reason:" + e.getMessage(), e);
            }
            throw new MessageRoutingException(e);
        }
    }
}