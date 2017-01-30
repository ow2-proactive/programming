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


/** Helper class to allows int/long to byte array conversion
 *
 * Theses functions do not depend on the endianess of the machine
 *
 * @since ProActive 4.3.0
 */
class TypeHelper {
    /** Converts the byte representation of a long into its value.
     *
     * @param a the byte array in which to find the byte representation
     * @param offset the offset in the byte array at which to find the byte representation
     * @return the represented long value
     */
    public static long byteArrayToLong(final byte[] buf, final int offset) {
        long ret = ((long) (buf[offset + 0] & 0xFF) << 56);
        ret |= ((long) (buf[offset + 1] & 0xFF) << 48);
        ret |= ((long) (buf[offset + 2] & 0xFF) << 40);
        ret |= ((long) (buf[offset + 3] & 0xFF) << 32);
        ret |= ((long) (buf[offset + 4] & 0xFF) << 24);
        ret |= ((long) (buf[offset + 5] & 0xFF) << 16);
        ret |= ((long) (buf[offset + 6] & 0xFF) << 8);
        ret |= ((long) (buf[offset + 7] & 0xFF));
        return ret;
    }

    /** Copies the byte representation of a long into a byte array starting at the given offset
     *
     * @param val the long to convert
     * @param a the byte array in which to copy the byte representation
     * @param offset the index of the array at which to start copying
     */
    public static void longToByteArray(final long val, final byte[] buf, final int offset) {
        buf[offset + 0] = (byte) ((val >> 56));
        buf[offset + 1] = (byte) ((val >> 48));
        buf[offset + 2] = (byte) ((val >> 40));
        buf[offset + 3] = (byte) ((val >> 32));
        buf[offset + 4] = (byte) ((val >> 24));
        buf[offset + 5] = (byte) ((val >> 16));
        buf[offset + 6] = (byte) ((val >> 8));
        buf[offset + 7] = (byte) ((val) >> 0);
    }

    /** converts the byte representation of an int into its value as an integer.
     *
     * @param buf the byte array in which to find the byte representation
     * @param offset the offset in the byte array at which to find the byte representation
     * @return the represented int value
     */

    public static int byteArrayToInt(final byte[] buf, final int offset) {
        int ret = (buf[offset + 0] << 24);
        ret |= (buf[offset + 1] & 0xFF) << 16;
        ret |= (buf[offset + 2] & 0xFF) << 8;
        ret |= (buf[offset + 3] & 0xFF);

        return ret;
    }

    /** Copies the byte representation of an int into a byte array starting at the given offset
     *
     * @param val the int to convert
     * @param buf the byte array in which to copy the byte representation
     * @param offset the index of the array at which to start copying
     */
    public static void intToByteArray(final int val, final byte[] buf, final int offset) {
        buf[offset + 0] = (byte) (val >>> 24);
        buf[offset + 1] = (byte) (val >>> 16);
        buf[offset + 2] = (byte) (val >>> 8);
        buf[offset + 3] = (byte) (val);
    }

    /** Converts the byte representation of a long into its value.
     *
     * @param a the byte array in which to find the byte representation
     * @param offset the offset in the byte array at which to find the byte representation
     * @return the represented long value
     */
    public static long channelBufferToLong(final ChannelBuffer buf, final int offset) {
        long ret = ((long) (buf.getByte(offset + 0) & 0xFF) << 56);
        ret |= ((long) (buf.getByte(offset + 1) & 0xFF) << 48);
        ret |= ((long) (buf.getByte(offset + 2) & 0xFF) << 40);
        ret |= ((long) (buf.getByte(offset + 3) & 0xFF) << 32);
        ret |= ((long) (buf.getByte(offset + 4) & 0xFF) << 24);
        ret |= ((long) (buf.getByte(offset + 5) & 0xFF) << 16);
        ret |= ((long) (buf.getByte(offset + 6) & 0xFF) << 8);
        ret |= ((long) (buf.getByte(offset + 7) & 0xFF));
        return ret;
    }

    /** Copies the byte representation of a long into a byte array starting at the given offset
     *
     * @param val the long to convert
     * @param a the byte array in which to copy the byte representation
     * @param offset the index of the array at which to start copying
     */
    public static void longToChannelBuffer(final long val, final ChannelBuffer buf, final int offset) {
        buf.setByte(offset + 0, (byte) ((val >> 56)));
        buf.setByte(offset + 1, (byte) ((val >> 48)));
        buf.setByte(offset + 2, (byte) ((val >> 40)));
        buf.setByte(offset + 3, (byte) ((val >> 32)));
        buf.setByte(offset + 4, (byte) ((val >> 24)));
        buf.setByte(offset + 5, (byte) ((val >> 16)));
        buf.setByte(offset + 6, (byte) ((val >> 8)));
        buf.setByte(offset + 7, (byte) ((val >> 0)));
    }

    /** converts the byte representation of an int into its value as an integer.
     *
     * @param buf the byte array in which to find the byte representation
     * @param offset the offset in the byte array at which to find the byte representation
     * @return the represented int value
     */

    public static int channelBufferToInt(final ChannelBuffer buf, final int offset) {
        int ret = (buf.getByte(offset + 0)) << 24;
        ret |= (buf.getByte(offset + 1) & 0xFF) << 16;
        ret |= (buf.getByte(offset + 2) & 0xFF) << 8;
        ret |= (buf.getByte(offset + 3) & 0xFF);

        return ret;
    }

    /** Copies the byte representation of an int into a byte array starting at the given offset
     *
     * @param val the int to convert
     * @param buf the byte array in which to copy the byte representation
     * @param offset the index of the array at which to start copying
     */
    public static void intToChannelBuffer(final int val, final ChannelBuffer buf, final int offset) {
        buf.setByte(offset + 0, (byte) ((val >> 24)));
        buf.setByte(offset + 1, (byte) ((val >> 16)));
        buf.setByte(offset + 2, (byte) ((val >> 8)));
        buf.setByte(offset + 3, (byte) ((val >> 0)));
    }
}
