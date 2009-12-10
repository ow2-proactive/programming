/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.messagerouting.message;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;
import org.objectweb.proactive.extra.messagerouting.protocol.TypeHelper;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.Field;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;


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
        }
        int badLen = ProActiveRandom.nextInt(lastLen);
        logger.debug("invalid length " + badLen);
        TypeHelper.intToByteArray(badLen, ret, Field.LENGTH.getOffset());
        return ret;
    }

    protected byte[] alterProtoId() {
        byte[] ret = this.msg.toByteArray();
        int badProto = nextIntNeq(Message.PROTOV1);
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
