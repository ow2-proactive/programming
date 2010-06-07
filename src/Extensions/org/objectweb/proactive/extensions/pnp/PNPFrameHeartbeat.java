/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pnp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.objectweb.proactive.extensions.pnp.exception.PNPMalformedMessageException;


/** A PNP Heartbeat frame
 *
 * An heartbeat frame is sent by the server to the client to report that the channel is still up.
 *
 * @since ProActive 4.3.0
 */
class PNPFrameHeartbeat extends PNPFrame {

    /**
     * Fields of the {@link PNPFrameHeartbeat} header.
     *
     * These fields are put after the {@link PNPFrame} header.
     */
    public enum Field {
        /** An heartbeat id unique by channel to help debugging */
        HEARTHBEAT_ID(8, Long.class);

        private int length;
        private int myOffset = 0;

        static private int totalOffset = 0;

        /* type is only informative */
        private Field(int length, Class<?> type) {
            this.length = length;
        }

        /** Length of the field in bytes */
        public long getLength() {
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

        /** Length of the fields defined by {@link PNPFrameCall} */
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
                case HEARTHBEAT_ID:
                    return "HEARTHBEAT_ID";
                default:
                    return super.toString();
            }
        }
    }

    private long readHearthbeatId(ChannelBuffer buf, int offset) {
        long hearthbeat = TypeHelper.channelBufferToLong(buf, offset + PNPFrame.Field.getTotalOffset() +
            Field.HEARTHBEAT_ID.getOffset());

        return hearthbeat;
    }

    final long hearthbeatId;

    public PNPFrameHeartbeat(long hearthbeatId) {
        super(PNPFrame.MessageType.HEARTBEAT);

        this.hearthbeatId = hearthbeatId;
    }

    public PNPFrameHeartbeat(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        super(buf, offset);

        this.hearthbeatId = readHearthbeatId(buf, offset);
    }

    @Override
    public ChannelBuffer toChannelBuffer() {
        byte[] header = new byte[PNPFrame.Field.getTotalOffset() + Field.getTotalOffset()];
        super.writeHeader(header, 0);
        TypeHelper.longToByteArray(this.hearthbeatId, header, PNPFrame.Field.getTotalOffset() +
            Field.HEARTHBEAT_ID.getOffset());

        return ChannelBuffers.wrappedBuffer(header);
    }

    @Override
    public String toString() {
        return super.toString() + Field.HEARTHBEAT_ID.toString() + ":" + this.hearthbeatId + ";";
    }
}
