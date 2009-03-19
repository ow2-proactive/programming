/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.objectweb.proactive.extra.messagerouting.protocol.message.DebugMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;
import org.objectweb.proactive.extra.messagerouting.router.Attachment;
import org.objectweb.proactive.extra.messagerouting.router.Router;


/** Asynchronous handler for {@link MessageType#DEBUG_}
 * 
 * @since ProActive 4.1.0
 */
public class ProcessorDebug extends Processor {
    DebugMessage message;
    Attachment attachment;
    Router router;

    public ProcessorDebug(ByteBuffer message, Attachment attachment, Router router) {
        this.attachment = attachment;
        this.router = router;

        try {
            this.message = new DebugMessage(message.array(), 0);
        } catch (IllegalArgumentException e) {
            logger.warn(e);
            this.message = null;
        }

    }

    @Override
    public void process() {

        switch (message.getErrorType()) {
            case DEB_DISCONNECT:
                disconnect();
                break;
            case DEB_NOOP:
                logger.info("noop message received " + message);
                break;
            default:
                logger.error("Unhandled message " + message);
                break;
        }
    }

    private void disconnect() {
        Field f;
        try {
            f = this.attachment.getClass().getDeclaredField("socketChannel");
            f.setAccessible(true);
            SocketChannel socketChannel = (SocketChannel) f.get(this.attachment);
            socketChannel.socket().close();
            socketChannel.close();
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
