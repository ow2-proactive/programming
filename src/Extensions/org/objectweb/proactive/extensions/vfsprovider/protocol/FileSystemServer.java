package org.objectweb.proactive.extensions.vfsprovider.protocol;

import java.io.IOException;


/**
 * Protocol definition made of two parts: {@link StreamOperations} and {@link FileOperations}.
 * <p>
 * Instances of this class are intended to work as remote objects and hence are thread-safe.
 * <p>
 * All paths are absolute and refer to a file system tree that root definition depends on a
 * particular implementation. Hence every access is limited to that "change rooted" file system.
 * Each path is expected to begin with UNIX styled <code>/</code> separator, although DOS like
 * separators <code>\</code> are allowed. Any violation of above mentioned rules cause that
 * {@link IOException} is thrown.
 */
public interface FileSystemServer extends StreamOperations, FileOperations {
}
