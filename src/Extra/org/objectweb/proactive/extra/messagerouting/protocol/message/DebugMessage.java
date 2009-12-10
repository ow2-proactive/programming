/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.TypeHelper;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;


public class DebugMessage extends DataMessage {

    public enum DebugType {
        DEB_DISCONNECT, DEB_NOOP;

        public byte[] toByteArray() {
            byte[] buf = new byte[4];
            TypeHelper.intToByteArray(this.ordinal(), buf, 0);
            return buf;
        }
    }

    final private DebugType debug;

    public DebugMessage(AgentID dstAgentID, long msgID, DebugType error) {
        super(MessageType.DEBUG_, null, dstAgentID, msgID, HttpMarshaller.marshallObject(error));
        this.debug = error;
    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     *
     * @param byteArray
     *            the byte array from which to read
     * @param offset
     *            the offset at which to find the message in the byte array
     * @throws MalformedMessageException
     */
    public DebugMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset);

        if (this.getType() != MessageType.DEBUG_) {
            throw new MalformedMessageException("Malformed" + MessageType.DEBUG_ + " message:" +
                "Invalid value for " + Message.Field.MSG_TYPE + " field:" + this.getType());
        }

        try {
            this.debug = (DebugType) HttpMarshaller.unmarshallObject(this.getData());
        } catch (ClassCastException e) {
            throw new MalformedMessageException("Malformed" + MessageType.DEBUG_ + " message:" +
                "Invalid error type:", e);
        }
    }

    public DebugType getErrorType() {
        return this.debug;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((debug == null) ? 0 : debug.hashCode());
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
        DebugMessage other = (DebugMessage) obj;
        if (debug == null) {
            if (other.debug != null)
                return false;
        } else if (!debug.equals(other.debug))
            return false;
        return true;
    }

}
