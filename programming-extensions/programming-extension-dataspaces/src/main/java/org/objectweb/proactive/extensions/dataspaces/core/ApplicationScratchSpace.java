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

import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


/**
 * Represents configured scratch data space (scratch for an application) and provides operations on
 * it.
 * <p>
 * Objects of this type represents configured scratch data space, with {@link SpaceInstanceInfo} and
 * mounting point {@link DataSpacesURI} defined - these can be used to register or unregister that
 * space in spaces directory. This object can be used to configure and get URI of scratch for an
 * Active Object, through {@link #getScratchForAO(Body)}.
 * <p>
 * Implementations of this interface are thread-safe. Scratch data space should be deconfigured by
 * {@link #close()} method. Instances of this class are typically managed by
 * {@link NodeScratchSpace} instances.
 *
 * @see NodeScratchSpace
 */
public interface ApplicationScratchSpace {

    /**
     * Returns DataSpacesURI of a scratch for a specified body of an ActiveObject.
     * <p>
     * Any call of this method results in URI of scratch for given Active Object that can be
     * considered as configured. First call of this method for an Active Object actually configures
     * that scratch. I.e. removes all already existing files in that scratch (if any exist) and
     * creates empty directory.
     *
     * @param body
     *            of an ActiveObject
     * @return URI of an ActiveObject's scratch that can be used for resolving files
     * @throws FileSystemException
     *             when any file system related exception occurs during scratch creation (can happen
     *             during first method call for some argument)
     */
    public DataSpacesURI getScratchForAO(Body body) throws FileSystemException;

    /**
     * Returns description of this scratch data space, that cane be used to register or unregister
     * this space in {@link NamingService}.
     * <p>
     * Instance stays unchanged during application execution (lifetime of this object).
     *
     * @return description of a scratch data space
     */
    public SpaceInstanceInfo getSpaceInstanceInfo();

    /**
     * Returns mounting point of this scratch data space, that can be used to register or unregister
     * this space in {@link NamingService}.
     * <p>
     * Mounting point instance stays unchanged during application execution (lifetime of this
     * object).
     *
     * @return URI of a scratch data space's mounting point
     */
    public DataSpacesURI getSpaceMountingPoint();

    /**
     * Closes any opened resources by this instance and removes scratch data space directory
     * content. Any subsequent calls to closed instance may have undefined results.
     * <p>
     * After this call, content of this scratch data space will not be available any more. However,
     * unregistering space from {@link NamingService} is a caller's responsibility.
     *
     * @throws FileSystemException
     *             when any file system related exception occurs
     */
    public void close() throws FileSystemException;
}
