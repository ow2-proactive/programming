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

import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


/**
 * Stores information needed to configure an instance of an input or output data space. It
 * introduces additional information required for input/output - name of a space to be configured.
 *
 * @see SpaceConfiguration
 */
public class InputOutputSpaceConfiguration extends SpaceConfiguration {

    private final String name;

    /**
     * This factory method is shorthand for
     * {@link #createConfiguration(String, String, String, String, SpaceType)} with input space as a
     * type.
     *
     * @param url
     *            Access URL to this space, used for accessing data from remote nodes. URL defines
     *            which protocol is used to access the data from remote node, and some additional
     *            information for protocol like path, sometimes user name and password. May be
     *            <code>null</code> when remote access is not specified yet.
     * @param path
     *            Local path to access input data. This path is local to host with hostname
     *            specified in <code>hostname</code> argument. May be <code>null</code> if there is
     *            no local access.
     * @param hostname
     *            Name of host where data are stored. It is always used in conjunction with a path
     *            attribute. Input data can be accessed locally on host with that name. May be
     *            <code>null</code> only if path is <code>null</code>.
     * @param name
     *            Name of input data space to be created, unique per target application. Note that
     *            {@link PADataSpaces#DEFAULT_IN_OUT_NAME} value is reserved for default input
     *            space. Can not be <code>null</code> .
     * @throws ConfigurationException
     *             when provided arguments doesn't form correct configuration (no access, no
     *             hostname for path, wrong space type)
     */
    public static InputOutputSpaceConfiguration createInputSpaceConfiguration(String url, String path,
            String hostname, String name) throws ConfigurationException {

        return new InputOutputSpaceConfiguration(url, path, hostname, SpaceType.INPUT, name);
    }

    public static InputOutputSpaceConfiguration createInputSpaceConfiguration(List<String> urls, String path,
            String hostname, String name) throws ConfigurationException {

        return new InputOutputSpaceConfiguration(urls, path, hostname, SpaceType.INPUT, name);
    }

    /**
     * This factory method is shorthand for
     * {@link #createConfiguration(String, String, String, String, SpaceType)} with output space as
     * a type.
     *
     * @param url
     *            Access URL to this space, used for accessing data from remote nodes. URL defines
     *            which protocol is used to access the data from remote node, and some additional
     *            information for protocol like path, sometimes user name and password. May be
     *            <code>null</code> when remote access is not specified yet.
     * @param path
     *            Local path to access output data. This path is local to host with hostname
     *            specified in <code>hostname</code> argument. May be <code>null</code> if there is
     *            no local access.
     * @param hostname
     *            Name of host where data are stored. It is always used in conjunction with a path
     *            attribute. Output data can be accessed locally on host with that name. May be
     *            <code>null</code> only if path is <code>null</code>.
     * @param name
     *            Name of output data space to be created, unique per target application. Note that
     *            {@link PADataSpaces#DEFAULT_IN_OUT_NAME} value is used for default input (output)
     *            space. Can not be <code>null</code>.
     * @throws ConfigurationException
     *             when provided arguments doesn't form correct configuration (no access, no
     *             hostname for path, wrong space type)
     */
    public static InputOutputSpaceConfiguration createOutputSpaceConfiguration(String url, String path,
            String hostname, String name) throws ConfigurationException {

        return new InputOutputSpaceConfiguration(url, path, hostname, SpaceType.OUTPUT, name);
    }

    public static InputOutputSpaceConfiguration createOutputSpaceConfiguration(List<String> urls,
            String path, String hostname, String name) throws ConfigurationException {

        return new InputOutputSpaceConfiguration(urls, path, hostname, SpaceType.OUTPUT, name);
    }

    /**
     * Creates input or output data space configuration. This configuration may be incomplete (see
     * {@link #isComplete()}), but at least one access method has to be specified - local or remote.
     *
     * @param url
     *            Access URL to this space, used for accessing data from remote nodes. URL defines
     *            which protocol is used to access the data from remote node, and some additional
     *            information for protocol like path, sometimes user name and password. May be
     *            <code>null</code> when remote access is not specified yet.
     * @param path
     *            Local path to access input (output) data. This path is local to host with hostname
     *            specified in <code>hostname</code> argument. May be <code>null</code> if there is
     *            no local access.
     * @param hostname
     *            Name of host where data are stored. It is always used in conjunction with a path
     *            attribute. Input (output) data can be accessed locally on host with that name. May
     *            be <code>null</code> only if path is <code>null</code>.
     * @param spaceType
     *            Input or output data space type.
     * @param name
     *            Name of input (output) data space to be created, unique per target application.
     *            Note that {@link PADataSpaces#DEFAULT_IN_OUT_NAME} value is used for default
     *            output space. Can not be <code>null</code>.
     * @throws ConfigurationException
     *             when provided arguments doesn't form correct configuration (no access, no
     *             hostname for path, wrong space type)
     */
    public static InputOutputSpaceConfiguration createConfiguration(String url, String path, String hostname,
            String name, SpaceType spaceType) throws ConfigurationException {
        return new InputOutputSpaceConfiguration(url, path, hostname, spaceType, name);
    }

    public static InputOutputSpaceConfiguration createConfiguration(List<String> urls, String path,
            String hostname, String name, SpaceType spaceType) throws ConfigurationException {
        return new InputOutputSpaceConfiguration(urls, path, hostname, spaceType, name);
    }

    private InputOutputSpaceConfiguration(final String url, final String path, final String hostname,
            final SpaceType spaceType, final String name) throws ConfigurationException {
        this(url != null ? Collections.singletonList(url) : null, path, hostname, spaceType, name);
    }

    private InputOutputSpaceConfiguration(final List<String> urls, final String path, final String hostname,
            final SpaceType spaceType, final String name) throws ConfigurationException {
        super(urls, path, hostname, spaceType);
        this.name = name;

        if (spaceType != SpaceType.INPUT && spaceType != SpaceType.OUTPUT)
            throw new ConfigurationException("Invalid space type for InputOutputSpaceConfiguration");

        if (name == null)
            throw new ConfigurationException("Name cannot be null");
    }

    /**
     * @return name of a space to be configured
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[type: ");

        sb.append(getType());
        sb.append("; ");

        sb.append("name: ");
        sb.append(name);
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
        sb.append(urls);
        sb.append(']');

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SpaceConfiguration))
            return false;

        if (!super.equals(obj)) {
            return false;
        }
        final InputOutputSpaceConfiguration other = (InputOutputSpaceConfiguration) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + name.hashCode();
    }
}