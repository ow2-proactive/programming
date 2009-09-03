package org.objectweb.proactive.extensions.vfsprovider.protocol;

import java.io.Serializable;


/**
 * An interface representing file related information such as type and file system properties. Note
 * that existing instance of this interface denotes that the file exists.
 */
public interface FileInfo extends Serializable {

    /**
     * Indicates type of a file.
     *
     * @return type, never <code>null</code>
     */
    public FileType getType();

    /**
     * Returns the length of the file. The return value is unspecified if this pathname denotes a
     * directory, and equals <code>0L</code> if file denotes a system dependent entity such as
     * device.
     *
     * @return size of a file measured in bytes
     */
    public long getSize();

    /**
     * Returns the "last modified time" property of a file.
     * <p>
     * Precision of the "last modified time" property is related to the particular file system,
     * although all platforms support file's last modification time to the nearest second.
     *
     * @return last-modified time, measured in milliseconds since the epoch (00:00:00 GMT, January
     *         1, 1970) truncated into platform dependent precision
     */
    public long getLastModifiedTime();

    /**
     * Denotes if a file can be read by an application.
     *
     * @return <code>true</code> if and only if the file can be read by the application
     */
    public boolean isReadable();

    /**
     * Denotes if a file can be written by an application.
     *
     * @return <code>true</code> if and only if the file can be written by the application
     */
    public boolean isWritable();

    /**
     * Denotes if a file is hidden in terms of underlying platform's rules (e.g. in UNIX like
     * systems a hidden file contains "<code>.</code>" prefix).
     *
     * @return <code>true</code> if and only if the file is hidden
     */
    public boolean isHidden();
}
