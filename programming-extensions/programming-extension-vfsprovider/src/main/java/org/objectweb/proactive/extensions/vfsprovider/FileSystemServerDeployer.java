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
package org.objectweb.proactive.extensions.vfsprovider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileName;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;
import org.objectweb.proactive.extensions.vfsprovider.server.FileSystemServerImpl;


/**
 * Deploys {@link FileSystemServer} with instance of {@link FileSystemServerImpl} implementation on
 * the local runtime and provides URL access methods.
 */
public final class FileSystemServerDeployer {

    private static final String FILE_SERVER_DEFAULT_NAME = "defaultFileSystemServer";

    /** URLs of the remote object */
    final private String[] urls;

    /** URLs of the root file exposed through that server */
    private ArrayList<String> vfsRootURLs = new ArrayList<String>();

    private FileSystemServerImpl fileSystemServer;

    private RemoteObjectExposer<FileSystemServerImpl> roe;

    /**
     * Deploys locally a FileSystemServer as a RemoteObject with a default name.
     *
     * @param rootPath the real path on which to bind the server
     * @param autoclosing
     * @throws IOException
     */
    public FileSystemServerDeployer(String rootPath, boolean autoclosing) throws IOException {
        this(FILE_SERVER_DEFAULT_NAME, rootPath, autoclosing);
    }

    /**
     * Deploys locally a FileSystemServer as a RemoteObject with a default name.
     *
     * @param rootPath the real path on which to bind the server
     * @param autoclosing
     * @param rebind true if the service must rebind an existing one, false otherwise.
     * @throws IOException
     */
    public FileSystemServerDeployer(String rootPath, boolean autoclosing, boolean rebind) throws IOException {
        this(FILE_SERVER_DEFAULT_NAME, rootPath, autoclosing, rebind);
    }

    /**
     * Deploys locally a FileSystemServer as a RemoteObject with a given name.
     *
     * @param name of deployed RemoteObject
     * @param rootPath the real path on which to bind the server
     * @param autoclosing
     * @throws IOException
     */
    public FileSystemServerDeployer(String name, String rootPath, boolean autoclosing) throws IOException {
        this(name, rootPath, autoclosing, false);
    }

    /**
     * Deploys locally a FileSystemServer as a RemoteObject with a given name.
     *
     * It will use all protocols defined in ProActiveConfiguration
     *
     * @param name of deployed RemoteObject
     * @param rootPath the real path on which to bind the server
     * @param autoclosing
     * @param rebind true if the service must rebind an existing one, false otherwise.
     * @throws IOException
     */
    public FileSystemServerDeployer(String name, String rootPath, boolean autoclosing, boolean rebind)
            throws IOException {
        this(name, rootPath, autoclosing, rebind, null);
    }

    /**
     * Deploys locally a FileSystemServer as a RemoteObject with a given name using the provided protocol.
     *
     * @param name of deployed RemoteObject
     * @param rootPath the real path on which to bind the server
     * @param autoclosing
     * @param rebind true if the service must rebind an existing one, false otherwise.
     * @param protocol RemoteObjectFactory protocol
     * @throws IOException
     */
    public FileSystemServerDeployer(String name, String rootPath, boolean autoclosing, boolean rebind, String protocol)
            throws IOException {
        createFolderIfNotExists(rootPath);
        fileSystemServer = new FileSystemServerImpl(rootPath);
        try {
            roe = PARemoteObject.newRemoteObject(FileSystemServer.class.getName(), this.fileSystemServer);
            if (protocol != null) {
                // if no specific protocol is specified we use the local configuration, maybe with multi-protocols
                roe.createRemoteObject(name, rebind, protocol);
            } else {
                roe.createRemoteObject(name, rebind);
            }
            urls = roe.getURLs();
        } catch (ProActiveException e) {
            // Ugly but createRemoteObject interface changed
            throw new IOException6("", e);
        }

        // Add the rootPath as a file url
        vfsRootURLs.add(new File(rootPath).toURI().toString());

        try {
            String[] rootUrls = ProActiveFileName.getServerVFSRootURLs(urls);
            vfsRootURLs.addAll(Arrays.asList(rootUrls));
        } catch (URISyntaxException e) {
            ProActiveLogger.logImpossibleException(ProActiveLogger.getLogger(Loggers.VFS_PROVIDER_SERVER), e);
            throw new ProActiveRuntimeException(e);
        } catch (UnknownProtocolException e) {
            ProActiveLogger.logImpossibleException(ProActiveLogger.getLogger(Loggers.VFS_PROVIDER_SERVER), e);
            throw new ProActiveRuntimeException(e);
        }

        if (autoclosing)
            fileSystemServer.startAutoClosing();
    }

    private void createFolderIfNotExists(String rootPath) {
        File rootFile = new File(rootPath);
        if (!rootFile.exists()) {
            boolean created = rootFile.mkdirs();
            if (!created) {
                throw new IllegalStateException("Error when trying to create directories inside the path " + rootPath +
                                                " , is it write-protected ?");
            }
        } else {
            if (!rootFile.isDirectory()) {
                throw new IllegalArgumentException("Provided folder is not a directory");
            }
        }
    }

    public FileSystemServerImpl getLocalFileSystemServer() {
        return this.fileSystemServer;
    }

    public FileSystemServer getRemoteFileSystemServer() throws ProActiveException {
        return (FileSystemServer) RemoteObjectHelper.generatedObjectStub(this.roe.getRemoteObject());
    }

    /**
     * <strong>internal use only!</strong>
     */
    public String[] getRemoteFileSystemServerURLs() {
        return this.urls;
    }

    /**
     * @return the main URL (suitable for VFS provider) of the server; In case of several protocols used, this is the URL of the default protocol.
     */
    public String getVFSRootURL() {
        // the url at index 0 is a file url pointing to the root
        // the url at index 1 is the url corresponding to the default protocol used by this server
        // all greater indexes correspond to additional protocols used
        return vfsRootURLs.get(1);
    }

    /**
     * @return an array of URLs (suitable for VFS provider) all pointing to the root file exposed by this provider server. One url per protocol used; suitable for VFS provider
     */
    public String[] getVFSRootURLs() {
        return vfsRootURLs.toArray(new String[0]);
    }

    /**
     * Unexport the remote object and stops the server.
     *
     * @throws ProActiveException
     */
    public void terminate() throws ProActiveException {
        if (roe != null) {
            roe.unexportAll();
            roe.unregisterAll();
            roe = null;
            fileSystemServer.stopServer();
            fileSystemServer = null;
        }
    }
}
