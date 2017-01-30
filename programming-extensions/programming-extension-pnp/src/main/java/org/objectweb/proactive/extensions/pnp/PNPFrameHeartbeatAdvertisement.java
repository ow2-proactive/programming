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
package org.objectweb.proactive.extensions.pnp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.objectweb.proactive.extensions.pnp.exception.PNPMalformedMessageException;


/** A PNP heartbeat advertisement frame
 *
 * A such frame is sent by the client to the server when the channel is opened. It
 * is the first frame exchanged.
 *
 * @since ProActive 4.3.0
 */
class PNPFrameHeartbeatAdvertisement extends PNPFrame {

    /**
     * Fields of the {@link PNPFrameHeartbeat} header.
     *
     * These fields are put after the {@link PNPFrame} header.
     */
    public enum Field {
        /** An heartbeat id unique by channel to help debugging */
        HEARTHBEAT_PERIOD(8, Long.class);

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
            /*
             * WARNING: RACY SINGLE-CHECK INTIALIZATION
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
            /*
             * WARNING: RACY SINGLE-CHECK INTIALIZATION
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
                case HEARTHBEAT_PERIOD:
                    return "HEARTHBEAT_PERIOD";
                default:
                    return super.toString();
            }
        }
    }

    private long readHearthbeatPeriod(ChannelBuffer buf, int offset) {
        long hearthbeat = TypeHelper.channelBufferToLong(buf,
                                                         offset + PNPFrame.Field.getTotalOffset() +
                                                              Field.HEARTHBEAT_PERIOD.getOffset());

        return hearthbeat;
    }

    final long heartbeatPeriod;

    public PNPFrameHeartbeatAdvertisement(long heartbeatPeriod) {
        super(PNPFrame.MessageType.HEARTBEAT_ADV);

        this.heartbeatPeriod = heartbeatPeriod;
    }

    public PNPFrameHeartbeatAdvertisement(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        super(buf, offset);

        this.heartbeatPeriod = readHearthbeatPeriod(buf, offset);
    }

    public long getHeartbeatPeriod() {
        return this.heartbeatPeriod;
    }

    @Override
    public ChannelBuffer toChannelBuffer() {
        byte[] header = new byte[PNPFrame.Field.getTotalOffset() + Field.getTotalOffset()];
        super.writeHeader(header, 0);
        TypeHelper.longToByteArray(this.heartbeatPeriod,
                                   header,
                                   PNPFrame.Field.getTotalOffset() + Field.HEARTHBEAT_PERIOD.getOffset());

        return ChannelBuffers.wrappedBuffer(header);
    }

    @Override
    public String toString() {
        return super.toString() + Field.HEARTHBEAT_PERIOD.toString() + ":" + this.heartbeatPeriod + ";";
    }
}
