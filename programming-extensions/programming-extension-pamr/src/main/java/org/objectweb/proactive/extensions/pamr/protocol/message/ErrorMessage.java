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
package org.objectweb.proactive.extensions.pamr.protocol.message;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;


/** A {@link MessageType#ERR_} message
 * 
 * Error message are used to notify error to the agent. Several {@link ErrorType} exist.
 * Each one indicate a special type of error.
 * 
 * Within the current implementation, {@link MessageType#ERR_} is a {@link DataMessage} with the payload
 * 	being an integer which identifies the error code.
 *
 * @since ProActive 4.1.0
 */
public class ErrorMessage extends DataMessage {

    public enum ErrorType {
        /** A client disconnected from the router 
         * 
         * This message is broadcasted to all the clients currently connected to the router
         * when a client disconnect.
         *
         * <ul>
         * 	<li>Recipient and Faulty {@link AgentID} must be set</li>
         *  <li>message ID is set by the router, but is not correlated to any other message.</li>
         * </ul>
         */
        ERR_DISCONNECTION_BROADCAST,
        /** Message cannot be routed due a disconnected recipient
         * 
         * This message is send when a {@link MessageType#DATA_REQUEST} cannot be
         * routed due to a disconnected recipient. An error is send to the sender with 
         * the following properties:
         * 
         * <ul>
         * 	<li>Recipient is the sender of the {@link MessageType#DATA_REQUEST}</li>
         *  <li>Faulty is the recipient of the {@link MessageType#DATA_REQUEST}</li>
         *  <li>message ID is the message ID of the {@link MessageType#DATA_REQUEST}</li>
         * </ul>
         * 
         * This error message is <b>never</b> send for a {@link MessageType#DATA_REPLY}.
         */
        ERR_NOT_CONNECTED_RCPT,
        /** Message cannot be routed due to a unknown recipient
         *
         * This message is send when a {@link MessageType#DATA_REQUEST} cannot be
         * routed due to a unknown recipient. An error is send to the sender with 
         * the following properties:
         * 
         * <ul>
         * 	<li>Recipient is the sender of the {@link MessageType#DATA_REQUEST}</li>
         *  <li>Faulty is the recipient of the {@link MessageType#DATA_REQUEST}</li>
         *  <li>message ID is the message ID of the {@link MessageType#DATA_REQUEST}</li>
         * </ul>
         * 
         * This error message is <b>never</b> send for a {@link MessageType#DATA_REPLY}.
         */
        ERR_UNKNOW_RCPT, // Signals that the router does not known the RCPT
        /** Client advertised an unknown {@link AgentID} on reconnection
         * 
         * This message is send when a client send a {@link MessageType#REGISTRATION_REQUEST}
         * with an unknown {@link AgentID}. A such error should never occurs and reflect
         * a bug in the router or in the agent. The error is send to the client with 
         * the following properties:
         *
         * <ul>
         * 	<li>Recipient and Faulty set to the bogus Agent ID</li>
         *  <li>message ID is the message ID of the {@link MessageType#REGISTRATION_REQUEST}</li>
         * </ul>
         */
        ERR_INVALID_AGENT_ID,

        /** Client advertised an unknown router ID on reconnection
         * 
         * This message is send when a client send a {@link MessageType#REGISTRATION_REQUEST}
         * with an unknown router ID. A such error happens when a router is restarted.
         * Existing clients try to reconnect the endpoint.
         */
        ERR_INVALID_ROUTER_ID,
        /** Client advertised a wrong magic cookie on (re)connection
         *
         * This message is send when a client send a {@link MessageType#REGISTRATION_REQUEST}
         * with a wrong magic cookie. If so, the client is not allowed to connect and
         * the connection is terminated.
         */
        ERR_WRONG_MAGIC_COOKIE,
        /** A corrupted message was received, and cannot be
         * properly treated by the receiver.
         *
         */
        ERR_MALFORMED_MESSAGE;

        /** Reverse map associating an error type to an ID  */
        final static Map<Integer, ErrorType> idToErrorType;
        static {
            // Can't populate idToErrorType from constructor since enums are initialized before
            // any static initializers are run. It is safe to do it from this static block
            idToErrorType = new HashMap<Integer, ErrorType>();
            for (ErrorType errorType : values()) {
                idToErrorType.put(errorType.ordinal(), errorType);
            }
        }

        public static ErrorType getErrorType(int value) {
            return idToErrorType.get(value);
        }

        @Override
        public String toString() {
            switch (this) {
                case ERR_DISCONNECTION_BROADCAST:
                    return "ERR_DISCONNECTION_BROADCAST";
                case ERR_INVALID_AGENT_ID:
                    return "ERR_INVALID_AGENT_ID";
                case ERR_INVALID_ROUTER_ID:
                    return "ERR_INVALID_ROUTER_ID";
                case ERR_NOT_CONNECTED_RCPT:
                    return "ERR_NOT_CONNECTED_RCPT";
                case ERR_UNKNOW_RCPT:
                    return "ERR_UNKNOW_RCPT";
                case ERR_MALFORMED_MESSAGE:
                    return "ERR_MALFORMED_MESSAGE";
                default:
                    return super.toString();
            }
        }
    }

    /** The type of this error message */
    final private ErrorType error;

    /** Read the error type from the payload field of a raw message
     * It is assumed that the payload of an error message
     * 	is four bytes long, containing an encoded int
     *
     * @throws IllegalArgumentException - if the payload contains an unrecognized error code
     * @throws IllegalArgumentException - if the payload is not a four-bytes buffer
     * */
    static public ErrorType readErrorType(byte[] payload) throws MalformedMessageException {
        if (payload.length != 4)
            throw new MalformedMessageException("The payload is not four-bytes long");

        int errorCode = TypeHelper.byteArrayToInt(payload, 0);
        ErrorType type = ErrorType.getErrorType(errorCode);
        if (type != null)
            return type;
        else
            throw new MalformedMessageException("Invalid value for the error code: " + errorCode);
    }

    /** Create an error message
     *
     * @param recipient 
     * 		The recipient of this error message. Can be null for {@link ErrorType#ERR_DISCONNECTION_BROADCAST}
     * @param faulty 
     * 		The client which caused the error message. Can be null.
     * @param msgID 
     * 		The ID of the message which caused the error or an unique ID generated by the router. 
     * @param error 
     * 		The error type
     */
    public ErrorMessage(ErrorType error, AgentID recipient, AgentID faulty, long msgID) {
        super(MessageType.ERR_, faulty, recipient, msgID, new byte[Integer.SIZE / 8]);
        // fill in the payload
        TypeHelper.intToByteArray(error.ordinal(), this.data, 0);
        this.error = error;
    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     *
     * @param byteArray
     *            the byte array from which to read
     * @param offset
     *            the offset at which to find the message in the byte array
     * @throws MalformedMessageException
     * 			If the buffer does not contain a valid error message
     */
    public ErrorMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset);

        if (this.getType() != MessageType.ERR_) {
            throw new MalformedMessageException("Malformed" + MessageType.ERR_ + " message:" +
                                                "Invalid value for the " + Message.Field.MSG_TYPE + " field:" +
                                                this.getType());
        }

        this.error = readErrorType(this.getData());
    }

    /** Return the type of the error*/
    public ErrorType getErrorType() {
        return this.error;
    }

    public AgentID getFaulty() {
        return this.getSender();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((error == null) ? 0 : error.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ErrorMessage other = (ErrorMessage) obj;
        if (error == null) {
            if (other.error != null)
                return false;
        } else if (!error.equals(other.error))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " Error code:" + this.error;
    }

}
