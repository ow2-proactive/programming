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
package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.TypeHelper;


/** A {@link MessageType#ERR_} message
 * 
 * Error message are used to notify error to the agent. Several {@link ErrorType} exist.
 * Each one indicate a special type of error.
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
        ERR_INVALID_AGENT_ID;

        public byte[] toByteArray() {
            byte[] buf = new byte[4];
            TypeHelper.intToByteArray(this.ordinal(), buf, 0);
            return buf;
        }
    }

    /** The type of this error message */
    final private ErrorType error;

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
        super(MessageType.ERR_, faulty, recipient, msgID, HttpMarshaller.marshallObject(error));
        this.error = error;
    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     *
     * @param byteArray
     *            the byte array from which to read
     * @param offset
     *            the offset at which to find the message in the byte array
     * @throws InstantiationException
     */
    public ErrorMessage(byte[] byteArray, int offset) throws IllegalArgumentException {
        super(byteArray, offset);

        if (this.getType() != MessageType.ERR_) {
            throw new IllegalArgumentException("Invalid message type " + this.getType());
        }

        try {
            this.error = (ErrorType) HttpMarshaller.unmarshallObject(this.getData());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid error type:" + e);
        }
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

}
