package org.objectweb.proactive.extensions.dataspaces.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


/**
 * Represents the data content of a file.
 * <p>
 * To read from a file, use the <code>InputStream</code> returned by {@link #getInputStream}.
 * <p>
 * To write to a file, use the <code>OutputStream</code> returned by {@link #getOutputStream}
 * method. This will create the file, and the parent folder, if necessary.
 * <p>
 * A file may have multiple InputStreams open at the sametime.
 *
 * @see DataSpacesFileObject#getContent
 */
public interface FileContent {
    /**
     * Returns the file which this is the content of.
     */
    DataSpacesFileObject getFile();

    /**
     * Determines the size of the file, in bytes.
     *
     * @return The size of the file, in bytes.
     * @throws FileSystemException
     *             If the file does not exist, or is being written to, or on error determining the
     *             size.
     */
    long getSize() throws FileSystemException;

    /**
     * Determines the last-modified timestamp of the file.
     *
     * @return The last-modified timestamp.
     * @throws FileSystemException
     *             If the file does not exist, or is being written to, or on error determining the
     *             last-modified timestamp.
     */
    long getLastModifiedTime() throws FileSystemException;

    /**
     * check if this file has open streams
     */
    public boolean isOpen();

    /**
     * The MIME Content-type meta information of a file.
     *
     * @return content-type or <code>null</code> if not available/not supported
     */
    public String getContentMIMEType();

    /**
     * The content encoding meta information of a file.
     *
     * @return content encoding or <code>null</code> if not available/not supported
     */
    public String getContentEncoding();

    /**
     * Retrieves the certificates if any used to sign this file or folder.
     *
     * @return The certificates, or an empty array if there are no certificates or the file does not
     *         support signing.
     * @throws FileSystemException
     *             If the file does not exist, or is being written.
     */
    List<Certificate> getCertificates() throws FileSystemException;

    /**
     * Returns an input stream for reading the file's content.
     * <p/>
     * <p>
     * There may only be a single input or output stream open for the file at any time.
     *
     * @return An input stream to read the file's content from. The input stream is buffered, so
     *         there is no need to wrap it in a <code>BufferedInputStream</code>.
     * @throws FileSystemException
     *             If the file does not exist, or is being read, or is being written, or on error
     *             opening the stream.
     */
    InputStream getInputStream() throws FileSystemException;

    /**
     * Returns an output stream for writing the file's content.
     * <p/>
     * If the file does not exist, this method creates it, and the parent folder, if necessary. If
     * the file does exist, it is replaced with whatever is written to the output stream.
     * <p/>
     * <p>
     * There may only be a single input or output stream open for the file at any time.
     *
     * @return An output stream to write the file's content to. The stream is buffered, so there is
     *         no need to wrap it in a <code>BufferedOutputStream</code>.
     * @throws FileSystemException
     *             If the file is read-only, or is being read, or is being written, or on error
     *             opening the stream.
     */
    OutputStream getOutputStream() throws FileSystemException;

    /**
     * Returns a stream for reading/writing the file's content.
     * <p/>
     * If the file does not exist, and you use one of the write* methods, this method creates it,
     * and the parent folder, if necessary. If the file does exist, parts of the file are replaced
     * with whatever is written at a given position.
     * <p/>
     * <p>
     * There may only be a single input or output stream open for the file at any time.
     *
     * @return a random access stream, never <code>null</code>
     * @throws FileSystemException
     *             If the file is read-only, or is being read, or is being written, or on error
     *             opening the stream.
     */
    public RandomAccessContent getRandomAccessContent(final RandomAccessMode mode) throws FileSystemException;

    /**
     * Returns an output stream for writing the file's content.
     * <p/>
     * If the file does not exist, this method creates it, and the parent folder, if necessary. If
     * the file does exist, it is replaced with whatever is written to the output stream.
     * <p/>
     * <p>
     * There may only be a single input or output stream open for the file at any time.
     *
     * @param bAppend
     *            true if you would like to append to the file
     * @return An output stream to write the file's content to. The stream is buffered, so there is
     *         no need to wrap it in a <code>BufferedOutputStream</code>.
     * @throws FileSystemException
     *             If the file is read-only, or is being read, or is being written, or on error
     *             opening the stream.
     */
    OutputStream getOutputStream(boolean bAppend) throws FileSystemException;

    /**
     * Closes all resources used by the content, including any open stream. Commits pending changes
     * to the file.
     * <p/>
     * <p>
     * This method is a hint to the implementation that it can release resources. This object can
     * continue to be used after calling this method.
     */
    void close() throws FileSystemException;

}
