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

import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


/**
 * Stores information for a base of concrete scratch space configuration -
 * {@link ScratchSpaceConfiguration}, which can derived from instance of this class.
 * <p>
 * Usually, scratch configuration is specified by providing access ways for temporary storage, often
 * with abstract hostname definition that need to be filled on target host. Also, access path/URL is
 * later appended with concrete subdirectory created for each runtime, node and application
 * identifier. That makes static configuration for host not complete until actual deployment takes
 * place. This class is intended to provide a way to store this static configuration and derive
 * actual scratch space configuration on target host from that instance.
 *
 * @see ScratchSpaceConfiguration
 */
public class BaseScratchSpaceConfiguration implements Serializable {
    /**
     *
     */

    public static final String HOSTNAME_VARIABLE_KEYWORD = "#{hostname}";

    private String url;

    private String path;

    /**
     * Creates base for a scratch space configuration.
     * <p>
     * At least one access way should be specified at this stage - local path or remote access URL.
     * Hostname is later filled at {@link #createScratchSpaceConfiguration(String...)}.
     * <p>
     * Remote access URL may contain special metavariable {@value #HOSTNAME_VARIABLE_KEYWORD} that
     * is later filled by localhost hostname.
     *
     * @param url
     *            Base access URL to scratch space, where subdirectories will be created. Used for
     *            accessing from remote nodes. URL defines which protocol is used to access the data
     *            from remote node, and some additional information for protocol like path,
     *            sometimes user name and password. This URL may contain special variable
     *            {@value #HOSTNAME_VARIABLE_KEYWORD} that is later filled with actual host name for
     *            caller, so scratch configuration definition may be more generic — sufficient to
     *            use in context of generic host configuration. May be <code>null</code> if remote
     *            access URL is not yet specified.
     * @param path
     *            Base of local path for scratch data space. This path is local to host where this
     *            base scratch configuration will be used. May be <code>null</code> if there is no
     *            local access specified.
     * @throws ConfigurationException
     *             When no access was specified.
     */
    public BaseScratchSpaceConfiguration(final String url, final String path) throws ConfigurationException {
        this.url = url;
        this.path = path;

        if (url == null && path == null)
            throw new ConfigurationException("No access specified (neither local, nor remote)");
    }

    /**
     * Creates base for a scratch space configuration with specified remote access, if it was not
     * specified yet.
     * <p>
     * Remote access URL may contain special metavariable {@value #HOSTNAME_VARIABLE_KEYWORD} that
     * is later filled by localhost hostname.
     *
     * @param url
     *            Base access URL to scratch space, where subdirectories will be created. Used for
     *            accessing from remote nodes. URL defines which protocol is used to access the data
     *            from remote node, and some additional information for protocol like path,
     *            sometimes user name and password. This URL may contain special variable
     *            {@value #HOSTNAME_VARIABLE_KEYWORD} that is later filled with actual host name for
     *            caller, so scratch configuration definition may be more generic — sufficient to
     *            use in context of generic host configuration. Cannot be <code>null</code>.
     * @return an instance of BaseScratchSpaceConfiguration with defined remote access
     * @throws ConfigurationException
     *             when remote access has been already specified or given URL is <code>null</code>
     * @see #BaseScratchSpaceConfiguration(String, String)
     */
    public BaseScratchSpaceConfiguration getWithRemoteAccess(final String url) throws ConfigurationException {
        if (this.url != null)
            throw new ConfigurationException(
                "Remote access has been already specified and cannot be redefined");
        if (url == null)
            throw new ConfigurationException("Cannot set remote access as an empty url");
        return new BaseScratchSpaceConfiguration(url, this.path);
    }

    /**
     * @return remote access URL with hostname metavariable filled with actual localhost hostname.
     *         May be <code>null</code>
     */
    public String getUrl() {
        if (url == null)
            return null;
        return url.replace(HOSTNAME_VARIABLE_KEYWORD, Utils.getHostname());
    }

    /**
     * @return local access path. May be <code>null</code>
     */
    public String getPath() {
        return path;
    }

    /**
     * Creates concrete scratch space configuration, derived from this base scratch space
     * configuration.
     * <p>
     * Hostname metavariable is replaced by localhost hostname in target URL, and provided
     * subdirectories are appended to local access path and remote access URL.
     *
     * @param subDirs
     *            Any number of subdirectories to be appended to target URL and path. Order of
     *            subdirectories responds to directories hierarchy and result path. None of it can
     *            be <code>null</code>
     * @return scratch space configuration derived from this base configuration.
     * @throws ConfigurationException
     *             when derived configuration is not correct (shouldn't happen)
     */
    public ScratchSpaceConfiguration createScratchSpaceConfiguration(final String... subDirs)
            throws ConfigurationException {
        ArrayList<String> urls = new ArrayList<String>();
        final String newUrl = Utils.appendSubDirs(getUrl(), subDirs);
        final String newPath = Utils.appendSubDirs(getPath(), subDirs);
        String localPathUrl = "file://" + (newPath.startsWith("/") ? "" : "/") + newPath.replace("\\", "/");
        if (!newUrl.equals(localPathUrl)) {
            urls.add(localPathUrl);
        }
        urls.add(newUrl);
        return new ScratchSpaceConfiguration(urls, newPath, Utils.getHostname());
    }
}
