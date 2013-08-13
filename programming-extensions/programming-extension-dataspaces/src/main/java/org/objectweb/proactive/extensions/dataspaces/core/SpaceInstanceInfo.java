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
package org.objectweb.proactive.extensions.dataspaces.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


/**
 * Stores complete description of data space instance, i.e. mounting point URI with information
 * contained there (type, application id...), and space access description (remote access URL,
 * optional local path and hostname).
 * <p>
 * Instances of this class are immutable, therefore thread-safe. <code>hashCode</code> and
 * <code>equals</code> methods are defined.
 */
public final class SpaceInstanceInfo implements Serializable {

    /**
     *
     */

    protected final List<String> urls = new ArrayList<String>();

    protected final String path;

    protected final String hostname;

    protected final DataSpacesURI mountingPoint;

    /**
     * Creates SpaceInstanceInfo for scratch data space.
     *
     * @param appid
     *            application identifier
     * @param runtimeId
     *            runtime identifier
     * @param nodeId
     *            node identifier
     * @param config
     *            scratch data space configuration; must be complete - with access URL specified
     * @throws ConfigurationException
     *             when provided information is not enough to build a complete space definition - no
     *             remote access URL is defined.
     * @see SpaceConfiguration#isComplete()
     */
    public SpaceInstanceInfo(long appid, String runtimeId, String nodeId, ScratchSpaceConfiguration config)
            throws ConfigurationException {
        this(config, DataSpacesURI.createScratchSpaceURI(appid, runtimeId, nodeId));
    }

    /**
     * Creates SpaceInstanceInfo for input/output data space.
     *
     * @param appid
     *            application identifier
     * @param config
     *            input or output data space configuration; must be complete - with access URL
     *            specified
     * @throws ConfigurationException
     *             when provided information is not enough to build a complete space definition - no
     *             remote access URL is defined.
     * @see SpaceConfiguration#isComplete()
     */
    public SpaceInstanceInfo(long appid, InputOutputSpaceConfiguration config) throws ConfigurationException {
        this(config, DataSpacesURI.createInOutSpaceURI(appid, config.getType(), config.getName()));
    }

    private SpaceInstanceInfo(final SpaceConfiguration config, final DataSpacesURI mountingPoint)
            throws ConfigurationException {
        if (!config.isComplete())
            throw new ConfigurationException(
                "Space configuration is not complete, no remote access URL provided");
        if (!mountingPoint.isSpacePartFullyDefined() || !mountingPoint.isSpacePartOnly()) {
            throw new RuntimeException(
                "Unexpectedly constructed mounting point URI does not have space part fully defined");
        }

        this.mountingPoint = mountingPoint;
        this.urls.addAll(config.getUrls());
        this.hostname = config.getHostname();
        this.path = config.getPath();
    }

    /**
     * Remote access URL. Always defined.
     *
     * @return remote access URL to this data space
     */
    public List<String> getUrls() {
        return urls;
    }

    /**
     * Returns local access path, that can be used for host with hostname as returned by
     * {@link #getHostname()}.
     * <p>
     * Local access path may not be defined.
     *
     * @return local access path; <code>null</code> if local access is undefined
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns hostname where local access path may be used. This hostname should be comparable to
     * {@link Utils#getHostname()}.
     *
     * @return hostname where local access path may be used; <code>null</code> if local access is
     *         undefined
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns mounting point URI of this data space.
     * <p>
     * Returned URI has always space part fully defined and nothing else.
     *
     * @return mounting point URI
     */
    public DataSpacesURI getMountingPoint() {
        return mountingPoint;
    }

    /**
     * Returns the name of a space, if such makes sense for that type of data space.
     *
     * @return name of a space; may be <code>null</code> for scratch data space
     */
    public String getName() {
        return mountingPoint.getName();
    }

    /**
     * @return data space type
     */
    public SpaceType getType() {
        return mountingPoint.getSpaceType();
    }

    /**
     * @return application id of data space
     */
    public long getAppId() {
        return mountingPoint.getAppId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mountingPoint.hashCode();
        result = prime * result + urls.hashCode();
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SpaceInstanceInfo))
            return false;

        final SpaceInstanceInfo other = (SpaceInstanceInfo) obj;
        if (mountingPoint == null) {
            if (other.mountingPoint != null)
                return false;
        } else if (!mountingPoint.equals(other.mountingPoint))
            return false;

        if (urls == null) {
            if (other.urls != null)
                return false;
        } else if (!urls.equals(other.urls))
            return false;

        if (hostname == null) {
            if (other.hostname != null)
                return false;
        } else if (!hostname.equals(other.hostname))
            return false;

        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[VFS URI: ");

        sb.append(mountingPoint);
        sb.append("; ");

        if (path == null) {
            sb.append(" no local-specific access");
        } else {
            sb.append("local access path: ");
            sb.append(path);
            sb.append(" at host: ");
            sb.append(hostname);
        }
        sb.append("; ");

        sb.append("remote access URLs: ");
        sb.append(urls.toString());
        sb.append(']');

        return sb.toString();
    }
}
