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

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;


/** A registration message
 * 
 * When a client connects to the router. It has to negociate its registration with 
 * {@link MessageType#REGISTRATION_REQUEST} and {@link MessageType#REGISTRATION_REPLY}
 * 
 * The first time a client connect to the router, it asks for an {@link AgentID}. If
 * the client is disconnected, it can reconnect to by advertising its {@link AgentID}.
 * 
 * @since ProActive 4.1.0
 */
public class ReloadConfigurationMessage extends Message {

    /**
     * Fields of the {@link RegistrationMessage} header.
     *
     * These fields are put after the {@link Message} header.
     */
    public enum Field {
        MAGIC_COOKIE(MagicCookie.COOKIE_SIZE, MagicCookie.class);

        private int length;

        private Class<?> type;

        private Field(int length, Class<?> type) {
            this.length = length;
            this.type = type;
        }

        public long getLength() {
            return this.length;
        }

        public int getOffset() {
            int offset = 0;
            // No way to avoid this iteration over ALL the field
            // There is no such method than Field.getOrdinal(x)
            for (Field field : values()) {
                if (field.ordinal() < this.ordinal()) {
                    offset += field.getLength();
                }
            }
            return offset;
        }

        public String getType() {
            return this.type.toString();
        }

        static public int getTotalOffset() {
            // OPTIM: Can be optimized with caching if needed
            int totalOffset = 0;
            for (Field field : values()) {
                totalOffset += field.getLength();
            }
            return totalOffset;
        }

        @Override
        public String toString() {
            switch (this) {
                case MAGIC_COOKIE:
                    return "MAGIC_COOKIE";
                default:
                    return super.toString();
            }
        }
    }

    /** The magic cookie */
    final private MagicCookie magicCookie;

    /** Create a registration message.
     * 
     * @param type
     * 		Type of the message {@link MessageType#REGISTRATION_REQUEST} or {@link MessageType#REGISTRATION_REPLY}
     * @param messageId
     * 		The message ID of the message. If {@link MessageType#REGISTRATION_REPLY}, then the message ID
     * 		must be the same than the correlated {@link MessageType#REGISTRATION_REQUEST}
     * @param agentID
     * 		The agentID or null
     * @param routerID
     * 		The router id
     */
    public ReloadConfigurationMessage(MagicCookie magicCookie) {
        super(MessageType.RELOAD_CONFIGURATION, 0);

        this.magicCookie = magicCookie;
        super.setLength(Message.Field.getTotalOffset() + Field.getTotalOffset());
    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     * @param byteArray the byte array from which to read
     * @param offset the offset at which to find the message in the byte array
     * @throws MalformedMessageException if the byte buffer does not contain a valid message
     */
    public ReloadConfigurationMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset, Field.getTotalOffset());

        try {
            this.magicCookie = readMagicCookie(byteArray, offset);
        } catch (MalformedMessageException e) {
            throw new MalformedMessageException("Malformed " + this.getType() + " message:" + e.getMessage());
        }
    }

    public MagicCookie getMagicCookie() {
        return this.magicCookie;
    }

    @Override
    public byte[] toByteArray() {
        int length = super.getLength();
        byte[] buff = new byte[length];

        super.writeHeader(buff, 0);
        System.arraycopy(this.magicCookie.getBytes(),
                         0,
                         buff,
                         Message.Field.getTotalOffset() + Field.MAGIC_COOKIE.getOffset(),
                         (int) Field.MAGIC_COOKIE.getLength());
        return buff;
    }

    static public MagicCookie readMagicCookie(byte[] byteArray, int offset) throws MalformedMessageException {
        byte[] cBuf = new byte[MagicCookie.COOKIE_SIZE];
        System.arraycopy(byteArray,
                         offset + Message.Field.getTotalOffset() + Field.MAGIC_COOKIE.getOffset(),
                         cBuf,
                         0,
                         (int) Field.MAGIC_COOKIE.getLength());
        return new MagicCookie(cBuf);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegistrationMessage other = (RegistrationMessage) obj;
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
