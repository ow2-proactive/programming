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
package org.objectweb.proactive.extensions.vfsprovider.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;


/**
 * VFS FileSystem implementation - representation of connection with one remote
 * {@link FileSystemServer}.
 *
 * @see ProActiveFileProvider
 */
public class ProActiveFileSystem extends AbstractFileSystem {
    private FileSystemServer server;

    protected ProActiveFileSystem(FileName rootName, FileSystemOptions fileSystemOptions) throws FileSystemException {
        super(rootName, null, fileSystemOptions);
        this.server = createServerStub();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addCapabilities(Collection caps) {
        caps.addAll(ProActiveFileProvider.CAPABILITIES);
    }

    @Override
    protected FileObject createFile(AbstractFileName name) throws Exception {
        return new ProActiveFileObject(name, this);
    }

    protected FileSystemServer getServer() throws FileSystemException {
        synchronized (this) {
            if (server == null) {
                server = createServerStub();
            }
            return server;
        }
    }

    // always called within synchronized (this)
    @Override
    protected void doCloseCommunicationLink() {
        server = null;
    }

    private FileSystemServer createServerStub() throws FileSystemException {
        final String serverURL = ((ProActiveFileName) getRootName()).getServerURL();
        final Object stub;
        try {
            stub = PARemoteObject.lookup(new URI(serverURL));
        } catch (URISyntaxException e) {
            throw new FileSystemException("Unexpected URL of file system server: " + serverURL, e);
        } catch (ProActiveException e) {
            throw new FileSystemException("Could not connect to file system server under specified URL: " + serverURL,
                                          e);
        }

        if (!(stub instanceof FileSystemServer)) {
            throw new FileSystemException("No valid FileSystemServer instance can be found under specified URL: " +
                                          serverURL);
        }
        return (FileSystemServer) stub;
    }
}
