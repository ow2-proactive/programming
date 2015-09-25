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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.pamr.message;

import org.junit.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;


/**
 * Testing the {@link MessageType#DATA_REQUEST}
 *  and {@link MessageType#DATA_REPLY} messages
 */
public class TestMessageData extends MessageFunctionalTest {

    /* Randomly construct valid data request then check all the fields.
     */
    @Test
    public void testDataRequest() {
        DataMessageGenerator msgGen = new DataMessageGenerator(MessageType.DATA_REQUEST);
        try {
            for (int i = 0; i < NB_CHECK; i++) {
                msgGen.buildValidMessage();
                msgGen.testFields();
                msgGen.testConversion();
                msgGen.testInvalidMessage();
            }
        } catch (MalformedMessageException e) {
            Assert.fail("There is a problem in the " + MessageType.DATA_REQUEST + " implementation:" +
                " the message " + msgGen.getMessage() + " cannot be reconstructed from its " +
                "raw byte form, because:" + e.getMessage());
        }
    }

    /* Randomly construct valid data reply then check all the fields.
     */
    @Test
    public void testDataReply() {
        DataMessageGenerator msgGen = new DataMessageGenerator(MessageType.DATA_REPLY);
        try {
            for (int i = 0; i < NB_CHECK; i++) {
                msgGen.buildValidMessage();
                msgGen.testFields();
                msgGen.testConversion();
                msgGen.testInvalidMessage();
            }
        } catch (MalformedMessageException e) {
            Assert.fail("There is a problem in the " + MessageType.DATA_REPLY + " implementation:" +
                " the message " + msgGen.getMessage() + " cannot be reconstructed from its " +
                "raw byte form, because:" + e.getMessage());
        }
    }

    /* Construct a data request with a null payload
     */
    @Test
    public void testRequestNullData() throws MalformedMessageException {
        AgentID srcAgent = new AgentID(ProActiveRandom.nextPosLong());
        AgentID dstAgent = new AgentID(ProActiveRandom.nextPosLong());
        long msgId = ProActiveRandom.nextPosLong();

        DataRequestMessage rq = new DataRequestMessage(srcAgent, dstAgent, msgId, null);
        Assert.assertNull(rq.getData());
        byte[] buf = rq.toByteArray();
        rq = new DataRequestMessage(buf, 0);
        Assert.assertEquals(0, rq.getData().length);
    }

    /* Construct a data reply with a null payload
     */
    @Test
    public void testReplyNullData() throws MalformedMessageException {
        AgentID srcAgent = new AgentID(ProActiveRandom.nextPosLong());
        AgentID dstAgent = new AgentID(ProActiveRandom.nextPosLong());
        long msgId = ProActiveRandom.nextPosLong();

        DataReplyMessage rp = new DataReplyMessage(srcAgent, dstAgent, msgId, null);
        Assert.assertNull(rp.getData());
        byte[] buf = rp.toByteArray();
        rp = new DataReplyMessage(buf, 0);
        Assert.assertEquals(0, rp.getData().length);
    }

    private class DataMessageGenerator extends MessageGenerator {

        private AgentID srcAgent;
        private AgentID dstAgent;
        private long msgId;
        private byte[] data;

        public DataMessageGenerator(MessageType type) {
            super(type);
        }

        @Override
        protected void buildValidMessage() {
            srcAgent = new AgentID(ProActiveRandom.nextPosLong());
            logger.debug("srcAgent " + srcAgent);
            dstAgent = new AgentID(ProActiveRandom.nextPosLong());
            logger.debug("dstAgent " + dstAgent);
            msgId = ProActiveRandom.nextPosLong();
            logger.debug("msgId " + msgId);
            data = new byte[ProActiveRandom.nextInt(100)];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) i;
            }
            logger.debug("data.length " + data.length);
            switch (type) {
                case DATA_REQUEST:
                    this.msg = new DataRequestMessage(srcAgent, dstAgent, msgId, data);
                    break;
                case DATA_REPLY:
                    this.msg = new DataReplyMessage(srcAgent, dstAgent, msgId, data);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void testFields() {
            DataMessage m = (DataMessage) this.msg;
            Assert.assertEquals(Message.PROTOV2, m.getProtoID());
            Assert.assertEquals(type, m.getType());
            Assert.assertEquals(msgId, m.getMessageID());
            Assert.assertEquals(srcAgent, m.getSender());
            Assert.assertEquals(dstAgent, m.getRecipient());

            for (int i = 0; i < m.getData().length; i++) {
                Assert.assertEquals((byte) i, m.getData()[i]);
            }
        }

        @Override
        protected void testConversion() throws MalformedMessageException {
            DataMessage m = (DataMessage) this.msg;
            byte[] buf = m.toByteArray();
            Assert.assertEquals(buf.length, m.getLength());

            DataMessage convMsg;
            switch (type) {
                case DATA_REQUEST:
                    convMsg = new DataRequestMessage(buf, 0);
                    break;
                case DATA_REPLY:
                    convMsg = new DataReplyMessage(buf, 0);
                    break;
                default:
                    return;
            }

            Assert.assertEquals(m.getLength(), convMsg.getLength());
            Assert.assertEquals(m.getProtoID(), convMsg.getProtoID());
            Assert.assertEquals(m.getType(), convMsg.getType());
            Assert.assertEquals(m.getMessageID(), convMsg.getMessageID());
            Assert.assertEquals(m.getSender(), convMsg.getSender());
            Assert.assertEquals(m.getRecipient(), convMsg.getRecipient());
            Assert.assertEquals(m.getData().length, convMsg.getData().length);

            for (int i = 0; i < m.getData().length; i++) {
                Assert.assertEquals(m.getData()[i], convMsg.getData()[i]);
            }

        }

        @Override
        protected void testInvalidMessage() {

            super.testInvalidMessage();

            // sender
            try {
                long invalidAgentId = nextLongLEQ(-1);
                logger.debug("invalid src " + invalidAgentId);
                DataMessage m = corruptAgentId(invalidAgentId, DataMessage.Field.SRC_AGENT_ID.getOffset());
                Assert.fail("Problem with " + type + " implementation: Attemp to reconstruct the message " +
                    m + " with a corrupted " + DataMessage.Field.SRC_AGENT_ID +
                    " from its raw byte array representation actually succeeded!");
            } catch (MalformedMessageException e) {
                // success
            }

            // recipient
            try {
                long invalidAgentId = nextLongLEQ(-1);
                logger.debug("invalid dst " + invalidAgentId);
                DataMessage m = corruptAgentId(invalidAgentId, DataMessage.Field.DST_AGENT_ID.getOffset());
                Assert.fail("Problem with " + type + " implementation: Attemp to reconstruct the message " +
                    m + " with a corrupted " + DataMessage.Field.DST_AGENT_ID +
                    " from its raw byte array representation actually succeeded!");
            } catch (MalformedMessageException e) {
                // success
            }

            // data payload integrity must be checked by the upper levels

        }

        private DataMessage corruptAgentId(long invalidAgentId, int fieldOffset)
                throws MalformedMessageException {
            byte[] corruptedMsg = msg.toByteArray();
            TypeHelper.longToByteArray(invalidAgentId, corruptedMsg, Message.Field.getTotalOffset() +
                fieldOffset);
            DataMessage ret = null;
            switch (type) {
                case DATA_REQUEST:
                    ret = new DataRequestMessage(corruptedMsg, 0);
                    break;
                case DATA_REPLY:
                    ret = new DataReplyMessage(corruptedMsg, 0);
                    break;
                default:
                    break;
            }
            return ret;
        }
    }
}
