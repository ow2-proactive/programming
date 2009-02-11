package org.objectweb.proactive.extra.messagerouting.protocol;

/** Helper class to allows int/long to byte array conversion
 *
 * Theses functions do not depend on the endianess of the machine
 * 
 * @since ProActive 4.1.0
 */
public class TypeHelper {

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
}
