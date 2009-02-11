package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.TypeHelper;


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
     * @throws InstantiationException
     */
    public DebugMessage(byte[] byteArray, int offset) throws IllegalArgumentException {
        super(byteArray, offset);

        if (this.getType() != MessageType.DEBUG_) {
            throw new IllegalArgumentException("Invalid message type " + this.getType());
        }

        try {
            this.debug = (DebugType) HttpMarshaller.unmarshallObject(this.getData());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid error type:" + e);
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
