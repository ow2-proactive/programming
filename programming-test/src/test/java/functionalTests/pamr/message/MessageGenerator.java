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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.Field;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationRequestMessage;


/**
 * Common interface for message functional test helpers
 */
public abstract class MessageGenerator {

    static final protected Logger logger = Logger.getLogger("testsuite");

    protected Message msg;

    protected final MessageType type;

    public MessageGenerator(MessageType type) {
        this.type = type;
    }

    public Message getMessage() {
        return msg;
    }

    // generate a valid Message
    protected abstract void buildValidMessage();

    // verify if the fields of the above-generated message are OK
    protected abstract void testFields();

    // convert the generated into a byte array; reconstruct the message from this byte array and check
    // if the fields are the same as the original message
    protected abstract void testConversion() throws MalformedMessageException;

    // generate invalid messages by putting random invalid values in message fields.
    // and try to reconstruct these corrupted messages from the byte array raw format
    protected void testInvalidMessage() {

        // length
        try {
            byte[] corruptedMsg = alterLength();
            Message m = construct(corruptedMsg);
            Assert.fail("Problem with " + type + " implementation: Attemp to reconstruct the message " + m +
                        " with a corrupted " + Message.Field.LENGTH + " field " +
                        " from its raw byte array representation actually succeeded!");
        } catch (MalformedMessageException e) {
            // success
        }

        // protoId
        try {
            byte[] corruptedMsg = alterProtoId();
            Message m = construct(corruptedMsg);
            Assert.fail("Problem with " + type + " implementation: Attemp to reconstruct the message " + m +
                        " with a corrupted " + Message.Field.PROTO_ID + " field " +
                        " from its raw byte array representation actually succeeded!");
        } catch (MalformedMessageException e) {
            // success
        }

        // type
        try {
            byte[] corruptedMsg = alterMessageType();
            Message m = construct(corruptedMsg);
            Assert.fail("Problem with " + type + " implementation: Attemp to reconstruct the message " + m +
                        " with a corrupted " + Message.Field.MSG_TYPE + " field " +
                        " from its raw byte array representation actually succeeded!");
        } catch (MalformedMessageException e) {
            // success
        }

    }

    private Message construct(byte[] corruptedMsg) throws MalformedMessageException {
        switch (type) {
            case REGISTRATION_REQUEST:
                return new RegistrationRequestMessage(corruptedMsg, 0);
            case REGISTRATION_REPLY:
                return new RegistrationReplyMessage(corruptedMsg, 0);
            case DATA_REQUEST:
                return new DataRequestMessage(corruptedMsg, 0);
            case DATA_REPLY:
                return new DataReplyMessage(corruptedMsg, 0);
            default:
                return null;
        }
    }

    protected byte[] alterLength() {
        byte[] ret = this.msg.toByteArray();
        // read prev length
        int lastLen = TypeHelper.byteArrayToInt(ret, Field.LENGTH.getOffset());
        if (type.equals(MessageType.DATA_REPLY) || type.equals(MessageType.DATA_REQUEST)) {
            lastLen = Message.Field.getTotalOffset() + DataMessage.Field.getTotalOffset();
        } else if (type.equals(MessageType.REGISTRATION_REPLY) || type.equals(MessageType.REGISTRATION_REQUEST)) {
            lastLen = Message.Field.getTotalOffset() + RegistrationMessage.Field.getTotalOffset();
        }
        int badLen = ProActiveRandom.nextInt(lastLen);
        logger.debug("invalid length " + badLen);
        TypeHelper.intToByteArray(badLen, ret, Field.LENGTH.getOffset());
        return ret;
    }

    protected byte[] alterProtoId() {
        byte[] ret = this.msg.toByteArray();
        int badProto = nextIntNeq(Message.PROTOV2);
        logger.debug("invalid protoId " + badProto);
        TypeHelper.intToByteArray(badProto, ret, Field.PROTO_ID.getOffset());
        return ret;
    }

    protected byte[] alterMessageType() {
        byte[] ret = this.msg.toByteArray();
        int lastType = TypeHelper.byteArrayToInt(ret, Field.MSG_TYPE.getOffset());
        int badType = nextIntNeq(lastType);
        logger.debug("invalid msgType " + badType);
        TypeHelper.intToByteArray(badType, ret, Field.MSG_TYPE.getOffset());
        return ret;
    }

    /** return a random int different of the specified value */
    private int nextIntNeq(int val) {
        int ret = val;
        while (ret == val)
            ret = ProActiveRandom.nextInt();
        return ret;
    }

    /** return a random long less than or equal to the specified value */
    protected long nextLongLEQ(long i) {
        long ret = ProActiveRandom.nextLong();
        if (ret == i)
            return ret;
        if (ret > i)
            ret = 2 * i - ret;
        return ret;
    }

}
