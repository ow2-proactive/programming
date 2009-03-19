/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.router.processor;

import java.nio.ByteBuffer;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;
import org.objectweb.proactive.extra.messagerouting.router.Client;
import org.objectweb.proactive.extra.messagerouting.router.RouterImpl;


/** Asynchronous handler for {@link MessageType#DATA_REPLY}
 * 
 * @since ProActive 4.1.0
 */
public class ProcessorDataReply extends Processor {

    final private ByteBuffer messageAsByteBuffer;
    final private RouterImpl router;

    public ProcessorDataReply(ByteBuffer messageAsByteBuffer, RouterImpl router) {
        this.messageAsByteBuffer = messageAsByteBuffer;
        this.router = router;
    }

    @Override
    public void process() {
        AgentID agentId = DataMessage.readRecipient(messageAsByteBuffer.array(), 0);
        Client destClient = this.router.getClient(agentId);

        if (destClient != null) {
            /* The recipient is known. Try to forward the message.
             * If the reply cannot be send now, we have to cache it to send it later.
             * We don't want to send a error message to the sender. Our goal is to unblock
             * the recipient which is waiting for the reply
             */
            destClient.sendMessageOrCache(this.messageAsByteBuffer);
        } else {
            /* The recipient is unknown.
             * 
             * We can't do better than dropping the reply. Notifying the sender is useless since
             * it will not unblock the recipient. 
             */
            try {
                Message message;
                message = new DataRequestMessage(messageAsByteBuffer.array(), 0);
                logger.error("Dropped invalid data reply: unknown recipient. " + message);
            } catch (IllegalArgumentException e) {
                ProActiveLogger.logImpossibleException(logger, e);
            }
        }
    }
}
