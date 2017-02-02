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
package functionalTests.pamr.router;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.router.Attachment;
import org.objectweb.proactive.extensions.pamr.router.Client;
import org.objectweb.proactive.extensions.pamr.router.MessageAssembler;
import org.objectweb.proactive.extensions.pamr.router.RouterInternal;

import functionalTests.FunctionalTest;


public class TestMessageAssembler extends FunctionalTest {
    static final private int NB_MESSAGE = 100;

    /*
     * This test create several messages and split them.
     *
     * Each message chunk is given to the MessageAssembler. After each message
     * we check that router.handleAsynchronously() was called once and only once.
     * We also check that the message is correct
     *
     */
    @Test
    public void testAssembler() throws MalformedMessageException {
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
            try {
                router.handleAsynchronouslyCalled(message);
            } catch (MalformedMessageException e) {
                Assert.fail("Badly rassembled message - corrupted message: " + e.getMessage());
            }

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

        public void handleAsynchronouslyCalled(Message expectedMessage) throws MalformedMessageException {
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
