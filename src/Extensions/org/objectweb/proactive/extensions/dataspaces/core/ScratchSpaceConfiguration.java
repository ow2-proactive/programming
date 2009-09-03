package org.objectweb.proactive.extensions.dataspaces.core;

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
}
