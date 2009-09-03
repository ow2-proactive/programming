package org.objectweb.proactive.extensions.vfsprovider.protocol;

/**
 * Represents a mode of a stream that is to be provided for
 * {@link StreamOperations#streamOpen(String, StreamMode)} method call.
 */
public enum StreamMode {

    /**
     * Indicates that stream is to be open for random reading only.
     */
    RANDOM_ACCESS_READ,

    /**
     * Indicates that stream is to be open for random reading and writing.
     */
    RANDOM_ACCESS_READ_WRITE,

    /**
     * Indicates that stream is to be open for sequential reading.
     */
    SEQUENTIAL_READ,

    /**
     * Indicates that stream is to be open for sequential writing.
     */
    SEQUENTIAL_WRITE,

    /**
     * Indicates that stream is to be open for sequential appending.
     */
    SEQUENTIAL_APPEND
}
