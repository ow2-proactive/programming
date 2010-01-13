/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.extra.messagerouting.remoteobject.message;

import java.io.Serializable;
import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.messagerouting.client.Agent;
import org.objectweb.proactive.extra.messagerouting.exceptions.MessageRoutingException;


/** Any kind of routed message.
 * 
 * @since ProActive 4.1.0 
 */

public abstract class MessageRoutingMessage implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 420L;

    static final Logger logger = ProActiveLogger.getLogger(Loggers.FORWARDING_REMOTE_OBJECT);

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

    protected boolean isAsynchronous = false;

    public MessageRoutingMessage(URI uri, Agent agent) {
        this.uri = uri;
        this.agent = agent;
        this.returnedObject = null;
    }

    /**
     * Processes the message.
     * @return an object as a result of the execution of the message
     */
    public abstract Object processMessage() throws Exception;

    /** Send the message to its recipient using the local agent
     * 
     * @throws MessageRoutingException if something bad happened when sending this message
     */
    public final void send() throws MessageRoutingException {
        try {
            byte[] bytes = HttpMarshaller.marshallObject(this);
            byte[] response = agent.sendMsg(this.uri, bytes, isAsynchronous);
            if (!isAsynchronous) {
                this.returnedObject = HttpMarshaller.unmarshallObject(response);
            }
        } catch (MessageRoutingException e) {
            logger.error("Failed to send message to " + this.uri, e);
            throw e;
        }
    }
}