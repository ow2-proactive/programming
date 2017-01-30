/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.dataspaces.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;


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
