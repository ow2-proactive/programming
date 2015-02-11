/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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

    protected ProActiveFileSystem(FileName rootName, FileSystemOptions fileSystemOptions)
            throws FileSystemException {
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
            throw new FileSystemException("Could not connect to file system server under specified URL: " +
                serverURL, e);
        }

        if (!(stub instanceof FileSystemServer)) {
            throw new FileSystemException(
                "No valid FileSystemServer instance can be found under specified URL: " + serverURL);
        }
        return (FileSystemServer) stub;
    }
}
