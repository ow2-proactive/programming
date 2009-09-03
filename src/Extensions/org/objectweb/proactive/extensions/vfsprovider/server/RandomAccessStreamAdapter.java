package org.objectweb.proactive.extensions.vfsprovider.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;


/**
 * Stream adapter for {@link RandomAccessFile} created from specified {@link File}, allowing the
 * random readings and/or writings. Two different static factory methods provided for different
 * access mode.
 */
public class RandomAccessStreamAdapter implements Stream {

    private final RandomAccessFile randomFile;

    private final boolean writable;

    /**
     * Create a stream adapter with specified {@link File} as {@link RandomAccessFile} in read only
     * mode.
     *
     * @param file
     *            to adapt
     * @return stream
     * @throws FileNotFoundException
     *             when specified file does not exist
     */
    public static Stream createRandomAccessRead(File file) throws FileNotFoundException {
        return new RandomAccessStreamAdapter(file, false);
    }

    /**
     * Create a stream adapter with specified {@link File} as {@link RandomAccessFile} in read and
     * write mode.
     *
     * @param file
     *            to adapt
     * @return stream
     * @throws FileNotFoundException
     *             when specified file does not exist
     */
    public static Stream createRandomAccessReadWrite(File file) throws FileNotFoundException {
        return new RandomAccessStreamAdapter(file, true);
    }

    private RandomAccessStreamAdapter(File file, boolean writable) throws FileNotFoundException {
        final String mode = writable ? "rw" : "r";

        this.randomFile = new RandomAccessFile(file, mode);
        this.writable = writable;
    }

    public void close() throws IOException {
        randomFile.close();
    }

    public long getLength() throws IOException {
        return randomFile.length();
    }

    public long getPosition() throws IOException {
        return randomFile.getFilePointer();
    }

    public byte[] read(int bytes) throws IOException, WrongStreamTypeException {
        final byte[] data = new byte[bytes];
        final int count = randomFile.read(data);

        if (count == -1)
            return null;
        if (count < bytes) {
            byte[] ret = new byte[count];
            System.arraycopy(data, 0, ret, 0, ret.length);
            return ret;
        }

        return data;
    }

    public void seek(long position) throws IOException {
        randomFile.seek(position);
    }

    public long skip(long bytes) throws IOException {
        long skippedTotal = 0;
        int skipped = Integer.MAX_VALUE;

        while (bytes >= Integer.MAX_VALUE && skipped == Integer.MAX_VALUE) {
            skipped = randomFile.skipBytes(Integer.MAX_VALUE);
            bytes -= skipped;
            skippedTotal += skipped;
        }
        skippedTotal += randomFile.skipBytes((int) (bytes % Integer.MAX_VALUE));
        return skippedTotal;
    }

    public void write(byte[] data) throws IOException, WrongStreamTypeException {
        assureIsWritable();
        randomFile.write(data);
    }

    public void flush() throws IOException, WrongStreamTypeException {
        assureIsWritable();
        randomFile.getChannel().force(true);
    }

    private void assureIsWritable() throws WrongStreamTypeException {
        if (!writable)
            throw new WrongStreamTypeException();
    }
}
