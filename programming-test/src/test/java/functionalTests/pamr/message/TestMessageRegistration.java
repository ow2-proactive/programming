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
package functionalTests.pamr.message;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationMessage.Field;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationRequestMessage;


/**
 * Testing the {@link MessageType#REGISTRATION_REQUEST}
 *  and {@link MessageType#REGISTRATION_REPLY} messages
 */
public class TestMessageRegistration extends MessageFunctionalTest {

    /*
     * Randomly construct valid registration request then check all the fields.
     */
    @Test
    public void testRegistrationRequest() {
        RegistrationMessageGenerator msgGen = new RegistrationMessageGenerator(MessageType.REGISTRATION_REQUEST);
        try {
            for (int i = 0; i < NB_CHECK; i++) {
                msgGen.buildValidMessage();
                msgGen.testFields();
                msgGen.testConversion();
                msgGen.testInvalidMessage();
            }
        } catch (MalformedMessageException e) {
            Assert.fail("There is a problem in the " + MessageType.REGISTRATION_REQUEST + " implementation:" +
                        " the message " + msgGen.getMessage() + " cannot be reconstructed from its " +
                        "raw byte form, because:" + e.getMessage());
        }

        msgGen.testExtremeValues();
    }

    /*
     * Randomly construct valid registration reply then check all the fields.
     */
    @Test
    public void testRegistrationReply() {
        RegistrationMessageGenerator msgGen = new RegistrationMessageGenerator(MessageType.REGISTRATION_REPLY);
        try {
            for (int i = 0; i < NB_CHECK; i++) {
                msgGen.buildValidMessage();
                msgGen.testFields();
                msgGen.testConversion();
                msgGen.testInvalidMessage();
            }
        } catch (MalformedMessageException e) {
            Assert.fail("There is a problem in the " + MessageType.REGISTRATION_REPLY + " implementation:" +
                        " the message " + msgGen.getMessage() + " cannot be reconstructed from its " +
                        "raw byte form, because:" + e.getMessage());
        }

        msgGen.testExtremeValues();
    }

    private class RegistrationMessageGenerator extends MessageGenerator {

        private AgentID agent;

        private long msgId;

        private long routerId;

        public RegistrationMessageGenerator(MessageType type) {
            super(type);
        }

        @Override
        protected void buildValidMessage() {
            agent = new AgentID(ProActiveRandom.nextPosLong());
            logger.debug("agent " + agent);
            msgId = ProActiveRandom.nextPosLong();
            logger.debug("msgId " + msgId);
            routerId = ProActiveRandom.nextPosLong();
            logger.debug("routerId " + routerId);
            switch (type) {
                case REGISTRATION_REQUEST:
                    this.msg = new RegistrationRequestMessage(agent, msgId, routerId, new MagicCookie());
                    break;
                case REGISTRATION_REPLY:
                    this.msg = new RegistrationReplyMessage(agent, msgId, routerId, new MagicCookie(), 0);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void testFields() {
            RegistrationMessage m = (RegistrationMessage) this.msg;
            Assert.assertEquals(Message.PROTOV2, m.getProtoID());
            Assert.assertEquals(type, m.getType());
            Assert.assertEquals(msgId, m.getMessageID());
            Assert.assertEquals(agent, m.getAgentID());
            Assert.assertEquals(routerId, m.getRouterID());
        }

        @Override
        protected void testConversion() throws MalformedMessageException {
            RegistrationMessage m = (RegistrationMessage) this.msg;
            byte[] buf = m.toByteArray();
            Assert.assertEquals(buf.length, m.getLength());

            RegistrationMessage convMsg;
            switch (type) {
                case REGISTRATION_REQUEST:
                    convMsg = new RegistrationRequestMessage(buf, 0);
                    break;
                case REGISTRATION_REPLY:
                    convMsg = new RegistrationReplyMessage(buf, 0);
                    break;
                default:
                    return;
            }

            Assert.assertEquals(m.getLength(), convMsg.getLength());
            Assert.assertEquals(m.getProtoID(), convMsg.getProtoID());
            Assert.assertEquals(m.getType(), convMsg.getType());
            Assert.assertEquals(m.getMessageID(), convMsg.getMessageID());
            Assert.assertEquals(m.getAgentID(), convMsg.getAgentID());

        }

        @Override
        protected void testInvalidMessage() {

            super.testInvalidMessage();

            // agentId
            try {
                long invalidAgentId = nextLongLEQ(-2);
                logger.debug("invalid agentId " + invalidAgentId);
                RegistrationMessage m = testAgentId(invalidAgentId);
                Assert.fail("Problem with " + type + " implementation: Attemp to reconstruct the message " + m +
                            " with a corrupted " + RegistrationMessage.Field.AGENT_ID +
                            " from its raw byte array representation actually succeeded!");
            } catch (MalformedMessageException e) {
                // success
            }

            // routerId
            try {
                long invalidRouterId = nextLongLEQ(-1);
                logger.debug("invalid routerId " + invalidRouterId);
                RegistrationMessage m = testRouterId(invalidRouterId);
                Assert.fail("Problem with " + type + " implementation: Attemp to reconstruct the message " + m +
                            " with a corrupted " + RegistrationMessage.Field.ROUTER_ID +
                            " from its raw byte array representation actually succeeded!");
            } catch (MalformedMessageException e) {
                // success
            }

        }

        /*
         * Extreme values:
         * - agentId == -1
         * - routerId == 0
         */
        protected void testExtremeValues() {

            long UNKNOWN_AGENT_ID = -1;
            // REG_REQ is allowed to have an unknown AgentID, but REG_REP is NOT
            try {
                logger.debug("agent " + UNKNOWN_AGENT_ID);
                testAgentId(UNKNOWN_AGENT_ID);
                if (type.equals(MessageType.REGISTRATION_REQUEST)) {
                    // is allowed
                } else {
                    Assert.fail("There is a problem in the " + MessageType.REGISTRATION_REPLY + " implementation:" +
                                " the " + RegistrationMessage.Field.AGENT_ID +
                                " field cannot be filled with the value " + UNKNOWN_AGENT_ID);
                }
            } catch (MalformedMessageException e) {
                if (type.equals(MessageType.REGISTRATION_REQUEST))
                    Assert.fail("There is a problem in the " + MessageType.REGISTRATION_REQUEST + " implementation:" +
                                " the value used to mark an unknown agent ID " + UNKNOWN_AGENT_ID +
                                " is not accepted as a valid value for the " + RegistrationMessage.Field.AGENT_ID +
                                " field");
                else {
                    // success
                }
            }

            long UNKNOWN_ROUTER_ID = 0;
            // REG_REQ is allowed to have an unknown RouterID, but REG_REP is NOT
            try {
                logger.debug("router " + UNKNOWN_ROUTER_ID);
                testRouterId(UNKNOWN_ROUTER_ID);
                if (type.equals(MessageType.REGISTRATION_REQUEST)) {
                    // is allowed
                } else {
                    Assert.fail("There is a problem in the " + MessageType.REGISTRATION_REPLY + " implementation:" +
                                " the " + RegistrationMessage.Field.ROUTER_ID +
                                " field cannot be filled with the value " + UNKNOWN_ROUTER_ID);
                }
            } catch (MalformedMessageException e) {
                if (type.equals(MessageType.REGISTRATION_REQUEST))
                    Assert.fail("There is a problem in the " + MessageType.REGISTRATION_REQUEST + " implementation:" +
                                " the value used to mark an unknown router ID " + UNKNOWN_ROUTER_ID +
                                " is not accepted as a valid value for the " + RegistrationMessage.Field.ROUTER_ID +
                                " field");
                else {
                    // success
                }
            }

        }

        private RegistrationMessage testAgentId(long invalidAgentId) throws MalformedMessageException {
            byte[] corruptedMsg = msg.toByteArray();
            TypeHelper.longToByteArray(invalidAgentId,
                                       corruptedMsg,
                                       Message.Field.getTotalOffset() + Field.AGENT_ID.getOffset());
            RegistrationMessage m = null;
            switch (type) {
                case REGISTRATION_REQUEST:
                    m = new RegistrationRequestMessage(corruptedMsg, 0);
                    break;
                case REGISTRATION_REPLY:
                    m = new RegistrationReplyMessage(corruptedMsg, 0);
                    break;
                default:
                    break;
            }
            return m;
        }

        private RegistrationMessage testRouterId(long invalidRouterId) throws MalformedMessageException {
            byte[] corruptedMsg = msg.toByteArray();
            TypeHelper.longToByteArray(invalidRouterId,
                                       corruptedMsg,
                                       Message.Field.getTotalOffset() + Field.ROUTER_ID.getOffset());
            RegistrationMessage m = null;
            switch (type) {
                case REGISTRATION_REQUEST:
                    m = new RegistrationRequestMessage(corruptedMsg, 0);
                    break;
                case REGISTRATION_REPLY:
                    m = new RegistrationReplyMessage(corruptedMsg, 0);
                    break;
                default:
                    break;
            }
            return m;
        }

    }
}
