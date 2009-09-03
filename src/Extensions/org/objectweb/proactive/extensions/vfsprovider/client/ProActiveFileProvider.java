package org.objectweb.proactive.extensions.vfsprovider.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;


/**
 * VFS Provider for ProActive file access protocol, as specified in {@link FileSystemServer}.
 * <p>
 * This provider supports only predefined schemes, as specified in
 * {@link ProActiveFileName#getAllVFSSchemes()}. This limitations is caused by usage of wide-used
 * protocols as a transport for {@link FileSystemServer}, while VFS manager does not allow to
 * specify multiple providers per scheme and differentiate between them (consider
 * {@link FileSystemServer} exposed as remote object through HTTP).
 * <p>
 * Note regarding content access implementation: input stream, output stream and random access read
 * provide internal buffering, while random access write does not.
 */
public class ProActiveFileProvider extends AbstractOriginatingFileProvider {

    static final Set<Capability> CAPABILITIES = Collections.unmodifiableSet(new HashSet<Capability>(Arrays
            .asList(new Capability[] { Capability.READ_CONTENT, Capability.WRITE_CONTENT,
                    Capability.RANDOM_ACCESS_READ, Capability.RANDOM_ACCESS_WRITE, Capability.APPEND_CONTENT,
                    Capability.LAST_MODIFIED, Capability.GET_LAST_MODIFIED,
                    Capability.SET_LAST_MODIFIED_FILE, Capability.SET_LAST_MODIFIED_FOLDER,
                    Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE,
                    Capability.LIST_CHILDREN, Capability.URI })));

    public ProActiveFileProvider() {
        setFileNameParser(ProActiveFileNameParser.getInstance());
    }

    @SuppressWarnings("unchecked")
    public Collection getCapabilities() {
        return CAPABILITIES;
    }

    @Override
    protected FileSystem doCreateFileSystem(FileName rootName, FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        return new ProActiveFileSystem(rootName, fileSystemOptions);
    }
}
