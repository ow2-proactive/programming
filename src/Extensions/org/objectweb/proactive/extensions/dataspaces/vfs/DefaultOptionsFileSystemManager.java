/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;


/**
 * Extension of DefaultFileSystemManager, providing default FileSystemOptions capability.
 * <p>
 * If some file is resolved without FileSystemOptions explicitly given and there is no baseFile
 * configured on manager that could be used as source of this options, resolve uses provided default
 * options for this manager.
 */
public class DefaultOptionsFileSystemManager extends DefaultFileSystemManager {
    private volatile FileSystemOptions defaultOptions;

    /**
     * Creates VFS manager with provided default options.
     *
     * @param defaultOptions
     *            default options to use in case options are not explicitly given in resolve or
     *            implicitly acquired from baseFile. May be <code>null</code>
     */
    public DefaultOptionsFileSystemManager(final FileSystemOptions defaultOptions) {
        super();
        this.defaultOptions = defaultOptions;
    }

    /**
     * @return default options this manager use in case options are not explicitly given in resolve
     *         or implicitly acquired from baseFile. May be <code>null</code>
     */
    public FileSystemOptions getDefaultOptions() {
        return defaultOptions;
    }

    /**
     * @param defaultOptions
     *            default options to use in case options are not explicitly given in resolve or
     *            implicitly acquired from baseFile. May be <code>null</code>
     */
    public void setDefaultOptions(FileSystemOptions defaultOptions) {
        this.defaultOptions = defaultOptions;
    }

    @Override
    public FileObject resolveFile(FileObject baseFile, String uri) throws FileSystemException {
        final FileSystemOptions options;
        if (baseFile == null)
            options = defaultOptions;
        else
            options = baseFile.getFileSystem().getFileSystemOptions();
        return resolveFile(baseFile, uri, options);
    }
}