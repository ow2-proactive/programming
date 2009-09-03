package org.objectweb.proactive.extensions.vfsprovider.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;


/**
 * Stream adapter for {@link OutputStream} of a {@link File}, allowing the sequential writings.
 */
public class OutputStreamAdapter implements Stream {

    private final OutputStream adaptee;

    /**
     * Adapt output stream of specified file, in append or normal mode.
     *
     * @param file
     *            of which stream is to be open
     * @param append
     *            indicates if open stream in append or normal mode
     * @throws FileNotFoundException
     *             when specified file does not exist
     */
    public OutputStreamAdapter(File file, boolean append) throws FileNotFoundException {
        adaptee = new FileOutputStream(file, append);
    }

    public void close() throws IOException {
        adaptee.close();
    }

    public long getLength() throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public long getPosition() throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public byte[] read(int bytes) throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public void seek(long position) throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public long skip(long bytes) throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public void write(byte[] data) throws IOException, WrongStreamTypeException {
        adaptee.write(data);
    }

    public void flush() throws IOException, WrongStreamTypeException {
        adaptee.flush();
    }
}
