/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pamr;

import java.net.Socket;

import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAPropertyString;
import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;


public class PAMRConfig implements PAPropertiesLoaderSPI {

    /** The address of the router to use. Must be set if message routing is enabled
    *
    * Can be FQDN or an IP address
    */
    static public PAPropertyString PA_NET_ROUTER_ADDRESS = new PAPropertyString(
        "proactive.net.router.address", false);

    /** The port of the router to use. Must be set if message routing is enabled
     *
     */
    static public PAPropertyInteger PA_NET_ROUTER_PORT = new PAPropertyInteger("proactive.net.router.port",
        false);

    /** The Socket Factory to use by the message routing protocol
     *
     */
    static public PAPropertyString PA_PAMR_SOCKET_FACTORY = new PAPropertyString(
        "proactive.communication.pamr.socketfactory", false, "plain");

    /**
     * Sockets used by the PAMR remote object factory connect to the remote server
     * with a specified timeout value. A timeout of zero is interpreted as an infinite timeout.
     * The connection will then block until established or an error occurs.
     */
    static public PAPropertyInteger PA_PAMR_CONNECT_TIMEOUT = new PAPropertyInteger(
        "proactive.communication.pamr.connect_timeout", false, 3000);

    /** The agent ID to use.
     *
     * This property can be set to obtain a given (and fixed) agent ID. This id must be declared
     * in the router configuration and must be between 0 and 4096.
     */
    static public PAPropertyInteger PA_PAMR_AGENT_ID = new PAPropertyInteger(
        "proactive.communication.pamr.agent.id", false);

    /** The Magic cookie to submit to the router
     *
     * If {@link #PA_PAMR_AGENT_ID} is set, then this property must also be set to be able
     * to use a reserved agent ID.
     *
     * If {@link #PA_PAMR_AGENT_ID} is not set, then this property can be set. But there is no
     * extra value to set it.
     *
     * A magic is a string up to 64 Unicode characters.
     */
    static public PAPropertyString PA_PAMR_AGENT_MAGIC_COOKIE = new PAPropertyString(
        "proactive.communication.pamr.agent.magic_cookie", false);

    /* ------------------------------------
     *  PAMR over SSH
     */

    /** this property identifies the location of RMISSH key directory */
    static public PAPropertyString PA_PAMRSSH_KEY_DIR = new PAPropertyString(
        "proactive.communication.pamrssh.key_directory", false);

    /** this property identifies the PAMR over SSH garbage collector period
     *
     * If set to 0, tunnels and connections are not garbage collected
     */
    static public PAPropertyInteger PA_PAMRSSH_GC_PERIOD = new PAPropertyInteger(
        "proactive.communication.pamrssh.gc_period", false);

    /** this property identifies the maximum idle time before a SSH tunnel or a connection is garbage collected */
    static public PAPropertyInteger PA_PAMRSSH_GC_IDLETIME = new PAPropertyInteger(
        "proactive.communication.pamrssh.gc_idletime", false);

    /** this property identifies the know hosts file location when using ssh tunneling
     *  if undefined, the default value is user.home property concatenated to SSH_TUNNELING_DEFAULT_KNOW_HOSTS
     */
    static public PAPropertyString PA_PAMRSSH_KNOWN_HOSTS = new PAPropertyString(
        "proactive.communication.pamrssh.known_hosts", false);

    /** Sock connect timeout, in ms
     *
     * The timeout to be used when a SSH Tunnel is opened. 0 is interpreted
     * as an infinite timeout. This timeout is also used for plain socket when try_normal_first is set to true
     *
     * @see Socket
     */
    static public PAPropertyInteger PA_PAMRSSH_CONNECT_TIMEOUT = new PAPropertyInteger(
        "proactive.communication.pamrssh.connect_timeout", false);

    // Not documented, temporary workaround until 4.3.0
    static public PAPropertyString PA_PAMRSSH_REMOTE_USERNAME = new PAPropertyString(
        "proactive.communication.pamrssh.username", false);

    // Not documented, temporary workaround until 4.3.0
    static public PAPropertyInteger PA_PAMRSSH_REMOTE_PORT = new PAPropertyInteger(
        "proactive.communication.pamrssh.port", false);

    public interface Loggers {

        // Forwarding
        static final public String PAMR = org.objectweb.proactive.core.util.log.Loggers.CORE + ".pamr";
        static final public String PAMR_MESSAGE = PAMR + ".message";
        static final public String PAMR_ROUTER = PAMR + ".router";
        static final public String PAMR_CLIENT = PAMR + ".client";
        static final public String PAMR_CLIENT_TUNNEL = PAMR_CLIENT + ".tunnel";
        static final public String PAMR_REMOTE_OBJECT = PAMR + ".remoteobject";
        static final public String PAMR_ROUTER_ADMIN = PAMR_ROUTER + ".admin";
        static final public String PAMR_CLASSLOADING = PAMR + ".classloading";
    }
}
