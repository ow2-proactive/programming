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

import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectProtocolFactoryRegistry;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;


/**
 * Representation of VFS file name for ProActive file access protocol, served by
 * {@link FileSystemServer}.
 * <p>
 * File name representation is a bit unusual for that protocol, although it bases on URL, with
 * particular scheme and specific path part interpretation.<br>
 * Scheme part of URL is one of ProActive Remote Object transport protocols (see
 * {@link RemoteObjectFactory} with prefix defined by {@link #VFS_PREFIX}. Prefix discriminates
 * scheme of pure transport protocol (like RMI) from scheme of ProActive file access protocol using
 * that transport (like file access over RMI). e.g. <code>paphttp://</code>, <code>paprmi://</code><br>
 * Path part of URL is divided into two subparts separated by
 * {@link #SERVICE_AND_FILE_PATH_SEPARATOR}. First part defines path to the service implementing
 * {@link FileSystemServer} - see {@link #getServicePath()}. While second part defines file path
 * within that remote file system - see {@link #getPath()} or {@link #getPathDecoded()}. e.g. of
 * complete path part of URL consisting of two subparts:
 * <code>/nodeX/fileSystemServer?proactive_vfs_provider_path=/dir/file.txt</code>.
 * <p>
 * Given that structure of file name/URL, having such an URL it is possible to determine URL of pure
 * {@link FileSystemServer} service and path within that server. e.g. of complete URL is:
 * <code>paprmi://host.com/nodeX/fileSystemServer?proactive_vfs_provider_path=/dir/file.txt</code>.
 * Corresponding URL of {@link FileSystemServer} service is
 * <code>rmi://host.com/nodeX/fileSystemServer</code>, while path described by that URL is
 * <code>/dir/file.txt</code>.
 */
public class ProActiveFileName extends GenericFileName {
    /**
     * String used in path part of URL, to separate service path and file path.
     */
    public static final String SERVICE_AND_FILE_PATH_SEPARATOR = "?proactive_vfs_provider_path=";

    /**
     * Prefix for scheme of transport protocol used by ProActive file access protocol.
     */
    public static final String VFS_PREFIX = "pap";

    /**
     * @return set of all prefixed schemes potentially used by ProActive file access protocol,
     *         basing on ProActive transport protocols.
     */
    public static Set<String> getAllVFSSchemes() {
        final Enumeration<String> enumaration = RemoteObjectProtocolFactoryRegistry.keys();
        final Set<String> result = new HashSet<String>();
        while (enumaration.hasMoreElements()) {
            result.add(getVFSSchemeForServerScheme(enumaration.nextElement()));
        }
        return result;
    }

    /**
     * Creates VFS URL of a root file for given {@link FileSystemServer} URL.
     *
     * @param serverURL
     *            {@link FileSystemServer} URL
     * @return VFS URL of root of remote file system exposed by provided server
     * @throws URISyntaxException
     *             when given URL does not conform to expected URL syntax (no scheme defined)
     * @throws UnknownProtocolException
     *             when scheme in given URL is not recognized as one of the known protocols scheme
     * @throws IllegalArgumentException
     *             when scheme of given URL is not supported, i.e. is not one of ProActive Remote
     *             Object protocols
     */
    public static String getServerVFSRootURL(String serverURL) throws URISyntaxException,
            UnknownProtocolException {
        final int dotIndex = serverURL.indexOf(':');
        if (dotIndex == -1) {
            throw new URISyntaxException(serverURL, "Could not find URL scheme");
        }
        final String schemeString = serverURL.substring(0, dotIndex);
        final String remainingPart = serverURL.substring(dotIndex);
        checkServerScheme(schemeString);
        return getVFSSchemeForServerScheme(schemeString) + remainingPart + SERVICE_AND_FILE_PATH_SEPARATOR +
            SEPARATOR_CHAR;
    }

    private static String getVFSSchemeForServerScheme(String serverScheme) {
        return VFS_PREFIX + serverScheme;
    }

    private static String getServerSchemeForVFSScheme(String vfsScheme) {
        if (!vfsScheme.startsWith(VFS_PREFIX)) {
            throw new IllegalArgumentException(vfsScheme + " is not a valid VFS server scheme");
        }
        final String strippedScheme = vfsScheme.substring(VFS_PREFIX.length());
        return strippedScheme;
    }

    private static void checkServerScheme(final String serverScheme) throws UnknownProtocolException {
        if (RemoteObjectProtocolFactoryRegistry.get(serverScheme) == null) {
            throw new UnknownProtocolException("Scheme " + serverScheme +
                " is not recognized as used by any of transport protocols");
        }
    }

    private static int getServerDefaultPortForVFSScheme(String vfsScheme) throws UnknownProtocolException {
        final String serverScheme;
        try {
            serverScheme = getServerSchemeForVFSScheme(vfsScheme);
        } catch (IllegalArgumentException x) {
            throw new UnknownProtocolException("Scheme " + vfsScheme +
                " is not properly formed ProVFS ProActive provider scheme");
        }
        checkServerScheme(serverScheme);
        final RemoteObjectFactory serverProtocolFactory = AbstractRemoteObjectFactory
                .getRemoteObjectFactory(serverScheme);
        return serverProtocolFactory.getPort();
    }

    private final String servicePath;

    private volatile String serverURL;
    private final Object serverURLSync = new Object();

    protected ProActiveFileName(String scheme, String hostName, int port, String userName, String password,
            String servicePath, String path, FileType type) throws UnknownProtocolException {
        super(scheme, hostName, port, getServerDefaultPortForVFSScheme(scheme), userName, password, path,
                type);
        if (servicePath == null || servicePath.length() == 0) {
            this.servicePath = ROOT_PATH;
        } else {
            this.servicePath = servicePath;
        }
    }

    @Override
    protected void appendRootUri(StringBuilder buffer, boolean addPassword) {
        super.appendRootUri(buffer, addPassword);
        buffer.append(servicePath);
        buffer.append(SERVICE_AND_FILE_PATH_SEPARATOR);
    }

    @Override
    public FileName createName(String absPath, FileType type) {
        try {
            return new ProActiveFileName(getScheme(), getHostName(), getPort(), getUserName(), getPassword(),
                servicePath, absPath, type);
        } catch (UnknownProtocolException e) {
            // it should never happen as it would be already thrown for this instance constructor
            throw new ProActiveRuntimeException(e);
        }
    }

    /**
     * @return path of a {@link FileSystemServer} service. Not including file path.
     */
    public String getServicePath() {
        return servicePath;
    }

    /**
     * @return URL of {@link FileSystemServer} for that file name.
     */
    public String getServerURL() {
        if (serverURL == null) {
            synchronized (serverURLSync) {
                if (serverURL == null) {
                    serverURL = createServerURL();
                }
            }
        }
        return serverURL;
    }

    private String createServerURL() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getServerSchemeForVFSScheme(getScheme()));
        buffer.append("://");
        appendCredentials(buffer, true);
        buffer.append(getHostName());
        if (getPort() != -1) {
            buffer.append(':');
            buffer.append(getPort());
        }
        buffer.append(servicePath);

        return buffer.toString();
    }
}
