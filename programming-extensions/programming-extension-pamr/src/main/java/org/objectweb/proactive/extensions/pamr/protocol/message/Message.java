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
package org.objectweb.proactive.extensions.pamr.protocol.message;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;


/** The common part of every ProActive Message Routing protocol.
 * 
 * The ProActive Message Routing protocol uses different type of messages defined
 * in the {@link MessageType} enumeration. All theses messages use the same base 
 * format defined in this classes. Subclasses can add additional fields or a payload.
 * They must.
 * 
 * Fields are defined in the {@link Field} enumeration.
 * 
 * @since ProActive 4.1.0
 */
public abstract class Message {

    /** Protocol version 1
     *
     * The initial PAMR protocol as release with ProActive 4.2.0
     */
    @Deprecated
    public static final int PROTOV1 = 1;

    /** Protocol version 2
     *
     * V2 adds an heartbeat mechanism and introduce a new message type
     * {@link HeartbeatMessage}. Since this message type in not handled
     * by clients supporting V1, the V2 is not backward compatible.
     *
     * If we want to support both V1 and V2 then the router must check
     * the version supported by the client and sent heartbeats only to
     * clients supporting V2.
     */
    public static final int PROTOV2 = 2;

    /** All the message types supported by the ProActive message routing protocol */
    /* ORDER MATTERS ! ordinal() is used to attribute an id to each message type */
    public enum MessageType {
        /** A registration request send by a client to the router */
        REGISTRATION_REQUEST,
        /** A registration reply send the router to a client */
        REGISTRATION_REPLY,
        /** A data message which encapsulate a {@link Request} */
        DATA_REQUEST,
        /** A data message which encapsulate a {@link SynchronousReplyImpl} */
        DATA_REPLY,
        /** An error notification. Send by the router to a client */
        ERR_,
        /** A message only used for debug and testing */
        DEBUG_,
        /** An heartbeat send by a client to the the router to check the connection */
        HEARTBEAT_CLIENT,
        /** An heartbeat send by the router to the clients to check the connection */
        HEARTBEAT_ROUTER,
        /** A message sent by the CLI tool, to ask the router to reload its configuration file*/
        RELOAD_CONFIGURATION
        /* That's all*/
        ;

        /** Reverse map associate a message type to an ID  */
        final static Map<Integer, MessageType> idToMessageType;
        static {
            // Can't populate idToMessageType from constructor since enums are initialized before 
            // any static initializers are run. It is safe to do it from this static block
            idToMessageType = new HashMap<Integer, MessageType>();
            for (MessageType messageType : values()) {
                idToMessageType.put(messageType.ordinal(), messageType);
            }
        }

        public static MessageType getMessageType(int value) {
            return idToMessageType.get(value);
        }

        @Override
        public String toString() {
            switch (this) {
                case REGISTRATION_REQUEST:
                    return "REG_REQ";
                case REGISTRATION_REPLY:
                    return "REG_REP";
                case DATA_REQUEST:
                    return "DATA_REQ";
                case DATA_REPLY:
                    return "DATA_REP";
                case ERR_:
                    return "ERR";
                case DEBUG_:
                    return "DBG";
                case HEARTBEAT_CLIENT:
                    return "HEARTBEAT_CLIENT";
                case HEARTBEAT_ROUTER:
                    return "HEARTBEAT_ROUTER";
                case RELOAD_CONFIGURATION:
                    return "RELOAD_CONFIGURATION";
                default:
                    return super.toString();
            }
        }
    }

    /** All the fields
     * 
     * This is the common header of all messages.
     */
    /* ORDER MATTERS ! ordinal() is used */
    public enum Field {
        /** Length of this message, all fields included
         * 
         * Value must be greater than getTotalOffset() and smaller than
         * {@link Integer}.MAX_INT
         */
        LENGTH(4, Integer.class),
        /** The protocol ID of this message
         * 
         * This field can be used to distinguish different protocol version.
         * 
         * Value is a strictly positive integer.
         */
        PROTO_ID(4, Integer.class),
        /** The type of this message.
         * 
         * ProActive Message Routing protocol support different types of message.
         *
         * Value is the ID of the message type as defined by {@link MessageType}
         */
        MSG_TYPE(4, Integer.class),
        /** The ID of this message
         * 
         * An ID is associated to each message. An unique ID is given to all
         * requests (DataRequest and Registration request). Reply and error message
         * reuse this ID to allow "transaction tracking". 
         */
        MSG_ID(8, Long.class);

        private int length;

        static private int totalOffset = 0;

        private int myOffset = 0;

        /* type is only informative */
        private Field(int length, Class<?> type) {
            this.length = length;
        }

        /** Length of the field in bytes */
        public int getLength() {
            return this.length;
        }

        /** Offset of the field in the message */
        public int getOffset() {
            /* WARNING: RACY SINGLE-CHECK INTIALIZATION
             * 
             * This method relies on the Java Memory Model specification to perform
             * a racy single-check initialization. 
             * DO NOT CHANGE THIS METHOD IF YOU DON'T KNOW WHAT IT IS.
             * 
             * See: Effective Java, chapter 10, Item 71: Lazy initialization
             * See: http://jeremymanson.blogspot.com/2008/12/benign-data-races-in-java.html
             */
            int tmpOffset = myOffset;
            if (tmpOffset == 0) {
                for (Field field : values()) {
                    if (field.ordinal() < this.ordinal()) {
                        tmpOffset += field.getLength();
                    }
                }

                myOffset = tmpOffset;
            }

            return myOffset;
        }

        /** Length of the fields defined by {@link Message} */
        static public int getTotalOffset() {
            /* WARNING: RACY SINGLE-CHECK INTIALIZATION
             * 
             * This method relies on the Java Memory Model specification to perform
             * a racy single-check initialization. 
             * DO NOT CHANGE THIS METHOD IF YOU DON'T KNOW WHAT IT IS.
             * 
             * See: Effective Java, chapter 10, Item 71: Lazy initialization
             * See: http://jeremymanson.blogspot.com/2008/12/benign-data-races-in-java.html
             */
            int tmpOffset = totalOffset;
            if (tmpOffset == 0) {
                for (Field field : values()) {
                    tmpOffset += field.getLength();
                }

                totalOffset = tmpOffset;
            }

            return totalOffset;
        }

        @Override
        public String toString() {
            switch (this) {
                case LENGTH:
                    return "LEN";
                case PROTO_ID:
                    return "PROTO_ID";
                case MSG_TYPE:
                    return "MSG_TYPE";
                case MSG_ID:
                    return "MSG_ID";
                default:
                    return super.toString();
            }
        }
    }

    /* @@@@@@@@@@@@@@@@@@@@ Static methods @@@@@@@@@@@@@@@@@@@@@@ */

    /** Construct a message from a byte array
     * 
     * @param buf 
     * 		a buffer which contains a message
     * @param offset
     * 		the offset at which the message begins  
     * @throws MalformedMessageException
     * 		If the buffer does not contain a valid message
     */
    public static Message constructMessage(byte[] buf, int offset) throws MalformedMessageException {
        // depending on the type of message, call a different constructor
        MessageType type = MessageType.getMessageType(TypeHelper.byteArrayToInt(buf,
                offset + Field.MSG_TYPE.getOffset()));

        switch (type) {
            case REGISTRATION_REQUEST:
                return new RegistrationRequestMessage(buf, offset);
            case REGISTRATION_REPLY:
                return new RegistrationReplyMessage(buf, offset);
            case DATA_REQUEST:
                return new DataRequestMessage(buf, offset);
            case DATA_REPLY:
                return new DataReplyMessage(buf, offset);
            case ERR_:
                return new ErrorMessage(buf, offset);
            case DEBUG_:
                return new DebugMessage(buf, offset);
            case HEARTBEAT_CLIENT:
                return new HeartbeatClientMessage(buf, offset);
            case HEARTBEAT_ROUTER:
                return new HeartbeatRouterMessage(buf, offset);
            case RELOAD_CONFIGURATION:
                return new ReloadConfigurationMessage(buf, offset);
            default:
                throw new MalformedMessageException("Unknown message type: " + type);
        }
    }

    /** Reads the length of a message 
     * 
     * @param buf 
     *		a buffer which contains a message
     * @param offset 
     * 		the offset at which the message begins  
     * @return The value of the length field of the message contained in buf at the given offset
     */
    public static int readLength(byte[] buf, int offset) {
        return TypeHelper.byteArrayToInt(buf, offset + Field.LENGTH.getOffset());
    }

    /** Reads the protocol ID of a message 
     * 
     * @param buf 
     *		a buffer which contains a message
     * @param offset 
     * 		the offset at which the message begins  
     * @return The value of the protocol ID field of the message contained in buf at the given offset
     */
    public static int readProtoID(byte[] buf, int offset) {
        return TypeHelper.byteArrayToInt(buf, offset + Field.PROTO_ID.getOffset());
    }

    /** Reads the message ID of a message 
     * 
     * @param buf 
     *		a buffer which contains a message
     * @param offset 
     * 		the offset at which the message begins  
     * @return The value of the message ID field of the message contained in buf at the given offset
     */
    public static long readMessageID(byte[] buf, int offset) {
        return TypeHelper.byteArrayToLong(buf, offset + Field.MSG_ID.getOffset());
    }

    /** Reads the type of a message 
     * 
     * @param buf 
     *		a buffer which contains a message
     * @param offset 
     * 		the offset at which the message begins  
     * @return The value of the message type field of the message contained in buf at the given offset
     * @throws MalformedMessageException if the message type field contains an invalid value
     */
    public static MessageType readType(byte[] byteArray, int offset) throws MalformedMessageException {
        int typeInt = TypeHelper.byteArrayToInt(byteArray, offset + Field.MSG_TYPE.getOffset());
        MessageType type = MessageType.getMessageType(typeInt);
        if (type != null)
            return type;
        else
            throw new MalformedMessageException("Invalid value for the " + Field.MSG_TYPE + " field:" +
                typeInt);
    }

    /** Length of this message */
    private int length;
    /** Protocol ID of this message */
    final private int protoId;
    /** Type of this message */
    final private MessageType type;
    /** ID of this message*/
    final private long messageId;

    /** Create the header of a message 
     * 
     * length and protocol ID fields will be automatically set when available
     * @param type
     * 		Type of the message
     * @param messageId
     * 		ID of the message
     */
    protected Message(MessageType type, long messageId) {
        this.type = type;
        this.protoId = PROTOV2;
        this.messageId = messageId;
    }

    /** Create the header of a message from a byte array
     * 
     * @param buf 
     *		a buffer which contains a message
     * @param offset 
     * 		the offset at which the message begins
     * @param fieldsSize
     * 		the size of the additional fields added by the messages. Can only be known within the particular implementations
     * @throws MalformedMessageException
     * 		If the buffer does not contain a valid message (proto ID, length etc.)
     */
    protected Message(byte[] buf, int offset, int fieldsSize) throws MalformedMessageException {

        this.length = readLength(buf, offset);
        this.protoId = readProtoID(buf, offset);
        this.type = readType(buf, offset);
        this.messageId = readMessageID(buf, offset);

        if (this.length < Field.getTotalOffset() + fieldsSize) {
            throw new MalformedMessageException("Malformed " + type.toString() + " message: " +
                "Invalid value for " + Field.LENGTH + " field:" + this.length);
        }

        if (this.protoId != PROTOV2) {
            throw new MalformedMessageException("Malformed " + type.toString() + " message: " +
                "Invalid value for " + Field.PROTO_ID + " field:" + this.protoId);
        }
    }

    /** Return the length of this message */
    public int getLength() {
        return length;
    }

    /** Set the length of this message 
     * 
     * This method must be called by subclass when constructing a message from
     * scratch.
     */
    protected void setLength(int length) {
        this.length = length;
    }

    /** Return the type of this message */
    public MessageType getType() {
        return this.type;
    }

    /** Return the message of this message */
    public long getMessageID() {
        return this.messageId;
    }

    /** Return the protocol ID of this message */
    public int getProtoID() {
        return this.protoId;
    }

    /** Convert this message into a byte array */
    public abstract byte[] toByteArray();

    /** Write the header of this message into buf
     * 
     * This method must be called by toByteArray() implementations.
     * 
     * @param buf
     * 		The buffer into the header must be put
     * @param offset
     * 		The offset a which the message will begin
     */
    protected void writeHeader(byte[] buf, int offset) {
        TypeHelper.intToByteArray(this.length, buf, offset + Field.LENGTH.getOffset());
        TypeHelper.intToByteArray(this.protoId, buf, offset + Field.PROTO_ID.getOffset());
        TypeHelper.intToByteArray(this.type.ordinal(), buf, offset + Field.MSG_TYPE.getOffset());
        TypeHelper.longToByteArray(this.messageId, buf, offset + Field.MSG_ID.getOffset());
    }

    @Override
    public String toString() {
        return Field.MSG_TYPE.toString() + ":" + this.type + ";" + Field.PROTO_ID.toString() + ":" +
            this.protoId + ";" + Field.MSG_ID.toString() + ":" + this.messageId + ";" +
            Field.LENGTH.toString() + ":" + this.length + ";";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        result = prime * result + (int) (messageId ^ (messageId >>> 32));
        result = prime * result + protoId;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Message other = (Message) obj;
        if (length != other.length)
            return false;
        if (messageId != other.messageId)
            return false;
        if (protoId != other.protoId)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
