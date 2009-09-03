/**
 *
 */
package org.objectweb.proactive.extensions.vfsprovider.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;


/**
 * Generic adapter of remotely accessed {@link FileSystemServer} to {@link InputStream} class.
 */
abstract class AbstractProActiveInputStreamAdapter extends InputStream {
    private static final Log log = LogFactory.getLog(AbstractProActiveInputStreamAdapter.class);

    private final byte[] SINGLE_BYTE_BUF = new byte[1];

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len + off > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        byte result[];
        try {
            try {
                result = getServer().streamRead(getStreamId(), len);
            } catch (StreamNotFoundException e) {
                reopenStream();
                result = getServer().streamRead(getStreamId(), len);
            }
        } catch (WrongStreamTypeException e) {
            throw Utils.generateAndLogIOExceptionWrongStreamType(log, e);
        } catch (StreamNotFoundException e) {
            throw Utils.generateAndLogIOExceptionStreamNotFound(log, e);
        }

        if (result == null) {
            return -1;
        }
        System.arraycopy(result, 0, b, off, result.length);
        notifyBytesRead(result.length);
        return result.length;
    }

    @Override
    public int read() throws IOException {
        if (read(SINGLE_BYTE_BUF) == -1) {
            return -1;
        }
        return SINGLE_BYTE_BUF[0];
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }

        try {
            long skippedBytes;
            try {
                skippedBytes = getServer().streamSkip(getStreamId(), n);
            } catch (StreamNotFoundException e) {
                reopenStream();
                skippedBytes = getServer().streamSkip(getStreamId(), n);
            }
            notifyBytesRead(skippedBytes);
            return skippedBytes;
        } catch (StreamNotFoundException e) {
            throw Utils.generateAndLogIOExceptionStreamNotFound(log, e);
        } catch (WrongStreamTypeException e) {
            throw Utils.generateAndLogIOExceptionWrongStreamType(log, e);
        }
    }

    protected abstract long getStreamId();

    protected abstract FileSystemServer getServer() throws FileSystemException;

    @Override
    public abstract void close() throws IOException;

    protected abstract void notifyBytesRead(long bytesNumber);

    protected abstract void reopenStream() throws IOException;
}
