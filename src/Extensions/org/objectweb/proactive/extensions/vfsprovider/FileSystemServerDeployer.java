/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.vfsprovider;

import java.io.IOException;
import java.net.URISyntaxException;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.RemoteObject;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileName;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;
import org.objectweb.proactive.extensions.vfsprovider.server.FileSystemServerImpl;


/**
 * Deploys {@link FileSystemServer} with instance of {@link FileSystemServerImpl} implementation on
 * the local runtime and provides URL access methods.
 */
public final class FileSystemServerDeployer {

    private static final String FILE_SERVER_DEFAULT_NAME = "defaultFileSystemServer";

    /** URL of the remote object */
    final private String url;

    /** URL of root file exposed through that server */
    private String vfsRootURL;

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
     * @param name of deployed RemoteObject
     * @param rootPath the real path on which to bind the server
     * @param autoclosing
     * @param rebind true if the service must rebind an existing one, false otherwise.
     * @throws IOException
     */
    public FileSystemServerDeployer(String name, String rootPath, boolean autoclosing, boolean rebind)
            throws IOException {
        fileSystemServer = new FileSystemServerImpl(rootPath);
        try {
            roe = PARemoteObject.newRemoteObject(FileSystemServer.class.getName(), this.fileSystemServer);
            roe.createRemoteObject(name, true);
            url = roe.getURL();
        } catch (ProActiveException e) {
            // Ugly but createRemoteObject interface changed
            throw new IOException("" + ProActiveLogger.getStackTraceAsString(e));
        }

        try {
            vfsRootURL = ProActiveFileName.getServerVFSRootURL(url);
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

    public FileSystemServerImpl getLocalFileSystemServer() {
        return this.fileSystemServer;
    }

    public FileSystemServer getRemoteFileSystemServer() throws ProActiveException {
        return (FileSystemServer) RemoteObjectHelper.generatedObjectStub(this.roe.getRemoteObject());
    }

    /**
     * <strong>internal use only!</strong>
     */
    public String getRemoteFileSystemServerURL() {
        return this.url;
    }

    /**
     * @return URL pointing to root file exposed by this provider server; suitable for VFS provider
     */
    public String getVFSRootURL() {
        return vfsRootURL;
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
