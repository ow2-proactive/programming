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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.core.util.converter.remote.ProActiveMarshaller;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.pnp.PNPAgent;
import org.objectweb.proactive.extra.pnp.PNPConfig;
import org.objectweb.proactive.extra.pnp.exception.PNPException;


/** Any kind of PNP message. */

abstract class PNPROMessage implements Serializable {
    static final Logger logger = ProActiveLogger.getLogger(PNPConfig.Loggers.PNP);

    /** The recipient of this message */
    final protected URI uri;

    /** The local agent to use to send the message
     *
     * Once the message has been sent, the agent is never used again. So this field can
     * safely be transient. Anyway, an external entity should be in charge of message sending
     * for a given runtime instead of embedding the logic in each message.
     */
    transient final protected PNPAgent agent;

    /** The response to this message
     *
     * <b>warning</b> send() and getReturnedObject() are <b>NOT thread safe</b>.
     * send will block until the response is received but getReturnedObject() is only
     * a getter. It will return null is the response has not yet been received.
     */
    protected Object returnedObject;

    protected boolean isAsynchronous = false;

    /** serialization
     *  This field is transient - it has significance only on this host
     * */
    private transient final ProActiveMarshaller marshaller;

    public PNPROMessage(URI uri, PNPAgent agent) {
        this.uri = uri;
        this.agent = agent;
        this.returnedObject = null;
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
     * @throws PNPException if something bad happened when sending this message
     */
    public final void send() throws PNPException {
        try {
            byte[] bytes = this.marshaller.marshallObject(this);

            // FIXME: Dynamic hearthbeat & service timeout

            long heartbeatPeriod = PNPConfig.PA_PNP_DEFAULT_HEARTBEAT.getValue();
            PNPFrameCall msgReq = new PNPFrameCall(agent.getCallId(), isAsynchronous, heartbeatPeriod, 0L,
                bytes);
            InputStream response = agent.sendMsg(uri, msgReq);
            if (!isAsynchronous) {
                try {
                    this.returnedObject = this.marshaller.unmarshallObject(response);
                } catch (IOException e) {
                    throw new PNPException("Failed to unmarshall the response", e);
                } catch (ClassNotFoundException e) {
                    throw new PNPException("Failed to unmarshall the response", e);
                }
            }
        } catch (IOException e) {
            throw new PNPException("Failed to marshall the call", e);
        } catch (PNPException e) {
            logger.error("Failed to send message to " + this.uri, e);
            throw e;
        }
    }
}