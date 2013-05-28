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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
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

    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES);

    protected final String path;

    protected final SpaceType spaceType;

    protected final String hostname;

    protected List<String> urls;

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
        this(url != null ? Collections.singletonList(url) : null, path, hostname, spaceType);
    }

    /**
     * Creates data space configuration instance. This configuration may be incomplete (see
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
     * @param spaceType
     *            Data space type.
     * @throws ConfigurationException
     *             when provided arguments doesn't form correct configuration (no access, no
     *             hostname for path)
     */
    protected SpaceConfiguration(final List<String> urls, final String path, final String hostname,
            final SpaceType spaceType) throws ConfigurationException {

        if (urls != null) {
            for (String url : urls) {
                try {
                    URI uriTest = new URI(url);
                } catch (URISyntaxException e) {
                    logger.warn("Malformed URI : " + url, e);
                }
            }
        }
        this.urls = urls;
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
        if (!localDefined && (urls == null || urls.size() == 0))
            throw new ConfigurationException("No access specified (neither local, nor remote)");
    }

    /**
     * @return remote access URL; may be <code>null</code>
     */
    public final List<String> getUrls() {
        return urls;
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
        return urls != null && urls.size() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SpaceConfiguration that = (SpaceConfiguration) o;

        if (hostname != null ? !hostname.equals(that.hostname) : that.hostname != null)
            return false;
        if (path != null ? !path.equals(that.path) : that.path != null)
            return false;
        if (spaceType != that.spaceType)
            return false;
        if (urls != null ? !urls.equals(that.urls) : that.urls != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (spaceType != null ? spaceType.hashCode() : 0);
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        result = 31 * result + (urls != null ? urls.hashCode() : 0);
        return result;
    }
}
