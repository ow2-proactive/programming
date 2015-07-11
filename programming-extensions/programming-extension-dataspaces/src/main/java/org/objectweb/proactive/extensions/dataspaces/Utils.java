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
package org.objectweb.proactive.extensions.dataspaces;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.Capability;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceType;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Static utilities methods.
 */
public class Utils {

    private static final Pattern WINDOWS_DRIVE_PATTERN = Pattern.compile("^[a-zA-Z]:\\\\.*");
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES);

    private Utils() {
    }

    public static void findFiles(DataSpacesFileObject baseDir, FileSelector selector,
            List<DataSpacesFileObject> results) throws FileSystemException {
        if (!selector.getIncludes().isEmpty()) {
            results.addAll(baseDir.findFiles(selector));
        }
    }

    /**
     * @see ProActiveInet#getHostname()
     * @return hostname of current Runtime
     */
    public static String getHostname() {
        // InetAddress.getLocalHost().getHostName();
        return ProActiveInet.getInstance().getHostname();
    }

    /**
     * @param node
     * @return an identifier of a Runtime of specified Node.
     */
    public static String getRuntimeId(Node node) {
        final ProActiveRuntime rt = node.getProActiveRuntime();
        return rt.getVMInformation().getName();
    }

    /**
     * @param node
     * @return an identifier of specified Node
     */
    public static String getNodeId(final Node node) {
        return node.getNodeInformation().getName();
    }

    /**
     * @param body
     * @return an identifier of a Body
     */
    public static String getActiveObjectId(Body body) {
        // shorten the oaid for path length reduction
        // TODO Handle properly collisions (PROACTIVE-1021)
        return String.valueOf(body.getID().hashCode());
    }

    /**
     * @return Body of an Active Object of a current active thread or HalfBody if caller is not an
     *         active object.
     */
    public static Body getCurrentActiveObjectBody() throws ProActiveRuntimeException {
        return PAActiveObject.getBodyOnThis();
    }

    /**
     * @return Node for current active thread or HalfBodies Node if caller is not an active object.
     * @throws ProActiveRuntimeException
     *             when internal PA exception on node acquisition
     */
    public static Node getCurrentNode() throws ProActiveRuntimeException {
        // TODO: Is it possible to do it in a better way?
        // We do not use PAActiveObject.getNode() to not crash for non-AO caller.
        try {
            final String nodeURL = getCurrentActiveObjectBody().getNodeURL();
            return NodeFactory.getNode(nodeURL);
        } catch (NodeException e) {
            ProActiveLogger.logImpossibleException(logger, e);
            throw new ProActiveRuntimeException("Cannot access local bodies or half-bodies node", e);
        }
    }

    /**
     * Determines local access URL for accessing some data, basing on provided remote access URL,
     * local access path and hostname specification.
     * <p>
     * If local access path is provided among with hostname, they are preferred over remote access
     * URL if local hostname determined by {@link #getHostname()} matches provided one.
     *
     * @param url
     *            mandatory remote access URL
     * @param path
     *            path for local access on host with hostname as specified in hostname argument; may
     *            be <code>null</code> when local access path is unspecified
     * @param hostname
     *            hostname where local access path is valid; can be <code>null</code> only when
     *            local access path is unspecified
     * @return local access URL that should be used for this host
     */
    public static String getLocalAccessURL(final String url, final String path, final String hostname) {
        if (hostname != null && hostname.equals(getHostname()) && path != null)
            return path;
        return url;
    }

    /**
     * Appends subdirectories to provided base location (local path or URL), handling file
     * separators (slashes) in appropriate way.
     * <p>
     * Both Unix- and Windows-like paths are supported and should be recognized by looking for
     * Windows-like drive letter at the beginning of a path.
     *
     * @param baseLocation
     *            Base location (path or URL) which is the root for appended subdirectories. Can be
     *            <code>null</code>.
     *            @param trailingSlash if we want a trailing slash to be added at the end
     * @param subDirs
     *            Any number of subdirectories to be appended to provided location. Order of
     *            subdirectories corresponds to directories hierarchy and result path. None of it
     *            can be <code>null</code> .
     * @return location with appended subdirectories with appropriate slashes (separators).
     *         <code>null</code> if <code>basePath</code> is <code>null</code>.
     */
    public static String appendSubDirs(final String baseLocation, boolean trailingSlash,
            final String... subDirs) {
        if (baseLocation == null)
            return null;

        final char separator;
        if (isWindowsPath(baseLocation))
            separator = '\\';
        else
            separator = '/';

        final StringBuilder sb = new StringBuilder(baseLocation);
        if (!baseLocation.endsWith(Character.toString(separator))) {
            sb.append(separator);
        }
        for (final String subDir : subDirs) {
            sb.append(subDir);
            sb.append(separator);
        }
        if (!trailingSlash) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String appendSubDirs(final String baseLocation, final String... subDirs) {
        return appendSubDirs(baseLocation, false, subDirs);
    }

    /**
     * Assert that given DataSpacesFileObject's data space has required capabilities. Throw an
     * ConfigurationException if it does not.
     *
     * @param expected
     *            set containing expected capabilities of the specified DataSpacesFileObject's data
     *            space.
     * @param fo
     *            specified DataSpacesFileObject's
     * @throws ConfigurationException
     *             when the DataSpacesFileObject's data space does not have one of expected
     *             capabilities
     */
    public static void assertCapabilitiesMatch(Set<Capability> expected, DataSpacesFileObject fo)
            throws ConfigurationException {

        for (Capability capability : expected) {
            if (!fo.hasSpaceCapability(capability))
                throw new ConfigurationException(
                    "File system used to access data does not support capability: " + capability);
        }
    }

    /**
     * Checks if the calling thread is owner of specified a scratch. If specified scratch URI is not
     * a valid one (with different type or without defined Active Object ID), <code>false</code> is
     * returned.
     *
     * @param uri
     *            of a scratch to check
     * @return <code>true</code> if the calling thread is owner of a scratch with specified valid
     *         URI, <code>false</code> in any other case
     */
    public static boolean isScratchOwnedByCallingThread(DataSpacesURI uri) {
        if (uri.getSpaceType() != SpaceType.SCRATCH)
            return false;

        final Body body = Utils.getCurrentActiveObjectBody();
        final String aoId = Utils.getActiveObjectId(body);
        final String aoIdFromURI = uri.getActiveObjectId();

        if (aoIdFromURI == null)
            return false;

        return aoIdFromURI.equals(aoId);
    }

    private static boolean isWindowsPath(String location) {
        return WINDOWS_DRIVE_PATTERN.matcher(location).matches();
    }
}
