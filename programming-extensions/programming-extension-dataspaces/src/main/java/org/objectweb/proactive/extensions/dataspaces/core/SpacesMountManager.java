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
package org.objectweb.proactive.extensions.dataspaces.core;

import java.util.Map;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.UserCredentials;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;


/**
 * Manages data spaces mountings and file accessing, connecting Data Spaces and access-protocol
 * worlds. Manager is intended to be shared among one application running on one Node, and many
 * Active Objects.
 * <p>
 * Manager is able to response queries for files in data spaces or just data spaces, providing
 * {@link DataSpacesFileObject} as a result. Returned object is associated with specific active
 * object. It is applicable for user level code with access limited to files with URIs suitable for
 * user path. Handling URIs not suitable for user path is not a responsibility of classes
 * implementing this interface.
 * <p>
 * To be able to serve requests for files and data spaces, SpaceMountManager is expected to use
 * {@link SpacesDirectory} as a source of information about data spaces, their mounting points and
 * access methods. Manager is expected to mount accessed spaces using best access way (local if
 * possible), preferably using lazy on-request strategy.
 * <p>
 * Capabilities of returned {@link DataSpacesFileObject} are limited to conform to general Data
 * Spaces guarantees, i.e. basing on file URI and Active Object id write access should be limited
 * for:
 * <ul>
 * <li>input data space - for every AO</li>
 * <li>AO's scratch space - for AO different that scratch's owner</li>
 * </ul>
 * <p>
 * Implementations of this class should be thread-safe. Also, subsequent requests for the same file
 * using the same manager should result in separate DataSpacesFileObject instances being returned,
 * so there is no concurrency issue related to returned DataSpacesFileObjects. They are not shared.
 * Each instance of manager must be closed using {@link #close()} method when it is not used
 * anymore.
 *
 * @see SpacesDirectory
 */
public interface SpacesMountManager {
    /**
     * Resolves query for URI within Data Spaces virtual tree, resulting in file-level access to
     * this place. Provided URI should be suitable for user path.
     * <p>
     * This call may block for a while, if {@link SpacesDirectory} need to be queried for data space
     * and/or data space may need to be mounted.
     *
     * @param queryUri
     *            Data Spaces URI to get access to
     * @param ownerActiveObjectId
     *            Id of active object requesting this file, that will become owner of returned
     *            {@link DataSpacesFileObject} instance. May be <code>null</code>, which corresponds
     *            to anonymous (unimportant) owner.
     * @param credentials
     *             credentials used to access the file (for implementations which support it)
     * @return {@link DataSpacesFileObject} instance that can be used to access this URI content;
     *         returned DataSpacesFileObject is not opened nor attached in any way; this
     *         DataSpacesFileObject instance will never be shared, i.e. individual instances are
     *         returned for subsequent queries (even the same queries).
     * @throws FileSystemException
     *             indicates exception during accessing local or remote file system, like mounting
     *             problems, I/O errors etc.
     * @throws SpaceNotFoundException
     *             when space with that query URI does not exists in SpacesDirectory
     * @throws IllegalArgumentException
     *             when provided queryUri is not suitable for user path
     * @see DataSpacesURI#isSuitableForUserPath()
     */
    DataSpacesFileObject resolveFile(final DataSpacesURI queryUri, final String ownerActiveObjectId,
            UserCredentials credentials) throws FileSystemException, SpaceNotFoundException;

    /**
     * Resolve query for URI without space part being fully defined, resulting in file-level access
     * to all data spaces that shares this common prefix. Requested result spaces must be suitable
     * for user path.
     * <p>
     * For any URI query, returned set contains all spaces (URIs) that match defined components in
     * queried URI, allowing them to have any values for undefined components.
     * <p>
     * e.g. for <code>vfs:///123/input/</code> query you may get
     * <code>vfs:///123/input/default/</code> and <code>vfs:///123/input/abc/</code> as a result,
     * but not <code>vfs:///123/output/</code>
     * <p>
     * This call may block for a while, as {@link SpacesDirectory} need to be queried for data
     * spaces and/or some data spaces may need to be mounted.
     *
     * @param queryUri
     *            Data Spaces URI to query for; must be URI without space part being fully defined,
     *            i.e. not pointing to any concrete data space; result spaces for that queries must
     *            be suitable for user path.
     * @param ownerActiveObjectId
     *            Id of active object requesting this files, that will become owner of returned
     *            {@link DataSpacesFileObject} instances. May be <code>null</code>, which
     *            corresponds to anonymous (unimportant) owner.
     * @param credentials
     *             credentials used to access the file (for implementations which support it)
     * @return map of data spaces URIs suitable for user path that match the query, pointing to
     *         {@link DataSpacesFileObject} instances that can be used to access their content;
     *         returned DataSpacesFileObject are not opened nor attached in any way; these
     *         DataSpacesFileObject instances will never be shared, i.e. another instances are
     *         returned for subsequent queries (even the same queries).
     * @throws FileSystemException
     *             indicates exception during accessing local or remote file system, like mounting
     *             problems, I/O errors etc.
     * @throws IllegalArgumentException
     *             when provided queryUri has space part fully defined or some of resolved spaces
     *             URIs are not suitable for user path
     * @see DataSpacesURI#isSpacePartFullyDefined()
     * @see DataSpacesURI#isSuitableForUserPath()
     */
    Map<DataSpacesURI, DataSpacesFileObject> resolveSpaces(final DataSpacesURI queryUri,
            final String ownerActiveObjectId, UserCredentials credentials) throws FileSystemException;

    /**
     * Closes this manager instance, releasing any opened resources.
     * <p>
     * Closing it indicates unmounting mounted data spaces. Any further access to already opened
     * DataSpacesFileObjects within these data spaces or any call of this instance may result in
     * undefined behavior for caller.
     * <p>
     * Subsequent calls to these method may result in undefined behavior.
     */
    void close();
}
