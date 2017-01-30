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

import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;


public class DebugMessage extends DataMessage {

    public enum DebugType {
        DEB_DISCONNECT,
        DEB_NOOP;

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
            throw new MalformedMessageException("Malformed" + MessageType.DEBUG_ + " message:" + "Invalid value for " +
                                                Message.Field.MSG_TYPE + " field:" + this.getType());
        }

        try {
            this.debug = (DebugType) HttpMarshaller.unmarshallObject(this.getData());
        } catch (ClassCastException e) {
            throw new MalformedMessageException("Malformed" + MessageType.DEBUG_ + " message:" + "Invalid error type:",
                                                e);
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
