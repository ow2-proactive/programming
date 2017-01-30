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

import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


/**
 * Stores information needed to configure an instance of a scratch data space.
 *
 * @see SpaceConfiguration
 */
public class ScratchSpaceConfiguration extends SpaceConfiguration {
    /**
         * Creates scratch data space configuration instance. This configuration may be incomplete (see
         * {@link #isComplete()}), but at least one access method has to be specified - local or remote.
         *
         * @param url
         *            Access URL to this space, used for accessing data from remote nodes. URL defines
         *            which protocol is used to access the data from remote node, and some additional
         *            information for protocol like path, sometimes user name and password. May be
         *            <code>null</code> when remote access is not specified yet.
         * @param path
         *            Local path to access data. This path is local to host with hostname specified in
         *            <code>hostname</code> argument. May be <code>null</code> if there is no local
         *            access.
         * @param hostname
         *            Name of host where data are stored. It is always used in conjunction with a path
         *            attribute. Input (output) data can be accessed locally on host with that name. May
         *            be <code>null</code> only if path is <code>null</code>.
         * @throws ConfigurationException
         *             when provided arguments doesn't form correct configuration (no access, no
         *             hostname for path)
         */
    public ScratchSpaceConfiguration(final String url, final String path, final String hostname)
            throws ConfigurationException {
        super(url, path, hostname, SpaceType.SCRATCH);
    }

    /**
     * Creates scratch data space configuration instance. This configuration may be incomplete (see
     * {@link #isComplete()}), but at least one access method has to be specified - local or remote.
     *
     * @param urls
     *            List of Access URL to this space, used for accessing data from remote nodes. each member URL of this list
     *            defines a protocol that can be used to access the data from remote node, and some additional
     *            information for protocol like path, sometimes user name and password. May be
     *            <code>null</code> when remote access is not specified yet.
     *            The order of the list defines the priority of protocols to use
     * @param path
     *            Local path to access data. This path is local to host with hostname specified in
     *            <code>hostname</code> argument. May be <code>null</code> if there is no local
     *            access.
     * @param hostname
     *            Name of host where data are stored. It is always used in conjunction with a path
     *            attribute. Input (output) data can be accessed locally on host with that name. May
     *            be <code>null</code> only if path is <code>null</code>.
     * @throws ConfigurationException
     *             when provided arguments doesn't form correct configuration (no access, no
     *             hostname for path)
     */
    public ScratchSpaceConfiguration(final List<String> urls, final String path, final String hostname)
            throws ConfigurationException {
        super(urls, path, hostname, SpaceType.SCRATCH);
    }
}
