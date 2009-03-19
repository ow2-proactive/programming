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
package functionalTests.messagerouting.router;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.router.Attachment;
import org.objectweb.proactive.extra.messagerouting.router.Client;
import org.objectweb.proactive.extra.messagerouting.router.MessageAssembler;
import org.objectweb.proactive.extra.messagerouting.router.RouterInternal;


public class TestMessageAssembler {
    static final private int NB_MESSAGE = 100;

    /* This test create several messages and split them.
     *
     * Each message chunk is given to the MessageAssembler. After each message
     * we check that router.handleAsynchronously() was called once and only once.
     * We also check that the message is correct
     *
     */
    @Test
    public void testAssembler() {
        FakeRouter router = new FakeRouter();
        Attachment attachment = new Attachment(null, null);
        MessageAssembler messageAssembler = new MessageAssembler(router, attachment);

        for (int i = 0; i < NB_MESSAGE; i++) {
            Message message = getMessage();
            List<ByteBuffer> byteBuffers = splitMessage(message);

            for (ByteBuffer byteBuffer : byteBuffers) {
                byteBuffer.flip();
                messageAssembler.pushBuffer(byteBuffer);
            }

            // Check is router.handleAsynchronously was called once and only once
            router.handleAsynchronouslyCalled(message);

        }
    }

    private Message getMessage() {
        AgentID srcId = new AgentID(ProActiveRandom.nextPosLong());
        AgentID dstId = new AgentID(ProActiveRandom.nextPosLong());
        long msgId = ProActiveRandom.nextPosLong();

        int data_length = ProActiveRandom.nextInt(1024 * 1024 * 1);
        byte data[] = new byte[data_length];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        Message msg = new DataReplyMessage(srcId, dstId, msgId, data);
        return msg;
    }

    private List<ByteBuffer> splitMessage(Message message) {
        List<ByteBuffer> byteBuffers = new LinkedList<ByteBuffer>();

        byte[] buffer = message.toByteArray();
        int index = 0;

        while (index < buffer.length) {
            int remainingBytes = buffer.length - index;

            ByteBuffer byteBuffer;
            if (remainingBytes < 10) {
                byteBuffer = ByteBuffer.allocate(remainingBytes);
            } else {
                byteBuffer = ByteBuffer.allocate(ProActiveRandom.nextInt(remainingBytes) + 1);
            }
            byteBuffer.put(buffer, index, byteBuffer.remaining());
            byteBuffers.add(byteBuffer);

            index += byteBuffer.capacity();
        }

        return byteBuffers;
    }

    private class FakeRouter extends RouterInternal {
        boolean handleAsynchronouslyCalled;
        ByteBuffer receivedByteBuffer;
        long attachmentId = -1;

        @Override
        public void handleAsynchronously(ByteBuffer message, Attachment attachment) {
            Assert.assertNull("Badly rassembled message, only one message was expected", receivedByteBuffer);

            this.receivedByteBuffer = message;
        }

        public void handleAsynchronouslyCalled(Message expectedMessage) {
            Assert.assertNotNull("handleAsynchronously not called. Assembler failed", receivedByteBuffer);

            Message receivedMessage = Message.constructMessage(receivedByteBuffer.array(), 0);
            Assert.assertEquals(expectedMessage, receivedMessage);

            Assert.assertEquals(expectedMessage.getLength(), receivedMessage.getLength());
            Assert.assertEquals(expectedMessage.getType(), receivedMessage.getType());
            // TODO Check the message is correct here

            this.receivedByteBuffer = null;
            this.attachmentId = -1;
        }

        @Override
        public InetAddress getInetAddr() {
            return null;
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public void stop() {
        }

        @Override
        public void addClient(Client client) {
        }

        @Override
        public Client getClient(AgentID agentId) {
            return null;
        }

    }
}
