/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.dataspaces.core;

import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


/**
 * Stores information needed to configure an instance of a data space, such as remote access URL,
 * local access path and hostname, space type. Subclasses may define some additional information.
 * <p>
 * It is possible to have an instance of this class without remote access URL specified, but this
 * instance is said to be incomplete (see {@link #isComplete()}) - can be used only temporary during
 * configuration process until remote access URL is specified.
 * <p>
 * Every instances of this class should provide <code>equals()</code> and <code>hashCode()</code>
 * methods.
 */
public abstract class SpaceConfiguration {
    protected final String path;

    protected final SpaceType spaceType;

    protected final String hostname;

    protected final String url;

    /**
     * Creates data space configuration instance. This configuration may be incomplete (see
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
     * @param spaceType
     *            Data space type.
     * @throws ConfigurationException
     *             when provided arguments doesn't form correct configuration (no access, no
     *             hostname for path)
     */
    protected SpaceConfiguration(final String url, final String path, final String hostname,
            final SpaceType spaceType) throws ConfigurationException {
        this.url = url;
        this.path = path;
        this.hostname = hostname;
        this.spaceType = spaceType;

        final boolean localDefined;
        if (path != null) {
            if (hostname == null)
                throw new ConfigurationException("Local path provided without hostname specified");
            localDefined = true;
        } else {
            localDefined = false;
        }
        if (!localDefined && url == null)
            throw new ConfigurationException("No access specified (neither local, nor remote)");
    }

    /**
     * @return remote access URL; may be <code>null</code>
     */
    public final String getUrl() {
        return url;
    }

    /**
     * @return local access path, valid on host with hostname as in <code>hostname</code>; may be
     *         <code>null</code>
     */
    public final String getPath() {
        return path;
    }

    /**
     * @return hostname where data can be accessed locally through local path; may be
     *         <code>null</code> if {@link #getPath()} is <code>null</code>
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * @return data space type
     */
    public final SpaceType getType() {
        return spaceType;
    }

    /**
     * Checks whether this space configuration completely defines data space, so it can be used to
     * create data space instance - at least space type and access URL must be specified.
     *
     * @return <code>true</code> if data space is completely defined, <code>false</code> if it miss
     *         remote access URL specification
     */
    public final boolean isComplete() {
        // remaining contract is guaranteed by the constructor
        return url != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + spaceType.hashCode();
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SpaceConfiguration))
            return false;

        SpaceConfiguration other = (SpaceConfiguration) obj;
        if (!spaceType.equals(other.spaceType)) {
            return false;
        }

        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }

        if (hostname == null) {
            if (other.hostname != null) {
                return false;
            }
        } else if (!hostname.equals(other.hostname)) {
            return false;
        }

        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }
}
