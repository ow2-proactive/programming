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
package org.objectweb.proactive.extensions.pamr.router.processor;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.message.DebugMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;
import org.objectweb.proactive.extensions.pamr.router.Attachment;
import org.objectweb.proactive.extensions.pamr.router.RouterImpl;


/** Asynchronous handler for {@link MessageType#DEBUG_}
 * 
 * @since ProActive 4.1.0
 */
public class ProcessorDebug extends Processor {
    Attachment attachment;

    public ProcessorDebug(ByteBuffer message, Attachment attachment, RouterImpl router) {
        super(message, router);
        this.attachment = attachment;
    }

    @Override
    public void process() throws MalformedMessageException {

        DebugMessage message = new DebugMessage(this.rawMessage.array(), 0);

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
