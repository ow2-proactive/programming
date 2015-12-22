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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pnp;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;


/**
 *
 * @since ProActive 4.3.0
 */
@PublicAPI
final public class PNPConfig implements PAPropertiesLoaderSPI {

    /**
     * The TCP port to bind to
     *
     * PNP binds to a local TCP port to accept incoming connections. By default a free port is randomly chosen
     * (port 0). By setting this property, one can force the TCP port.
     */
    static final public PAPropertyInteger PA_PNP_PORT = new PAPropertyInteger("proactive.pnp.port", false, 0);

    /**
     * The default heartbeat period (in milliseconds)
     *
     * PNP offers an heartbeat mechanism to detect network failure. If set to 0 hearthbeats are disabled and
     * network failure will not be detected before the TCP timeout which can be quite long.
     *
     * Setting this value too low impacts the network performance. It is not recommended to use a value
     * lower than 500 milliseconds.
     *
     */
    static final public PAPropertyInteger PA_PNP_DEFAULT_HEARTBEAT = new PAPropertyInteger(
        "proactive.pnp.default_heartbeat", false, 60 * 1000);

    /**
     * Channel garbage collection timeout (in milliseconds)
     *
     * Idle channels are garbage collected after a given timeout. If a channel is unused since this value then
     * the channel will be garbage collected.
     */
    static final public PAPropertyInteger PA_PNP_IDLE_TIMEOUT = new PAPropertyInteger(
        "proactive.pnp.idle_timeout", false, 600 * 1000);

    private int port;
    private int idleTimeout;
    private int defaultHeartbeat;

    public PNPConfig() {
        this.port = 0;
        this.idleTimeout = 600 * 1000;
        this.defaultHeartbeat = 60 * 1000;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public void setDefaultHeartbeat(int defaultHeartbeat) {
        this.defaultHeartbeat = defaultHeartbeat;
    }

    public int getPort() {
        return port;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public int getDefaultHeartbeat() {
        return defaultHeartbeat;
    }

    public interface Loggers {
        static final public String PNP = org.objectweb.proactive.core.util.log.Loggers.CORE + ".pnp";
        static final public String PNP_HANDLER_SERVER = PNP + ".handler.server";
        static final public String PNP_HANDLER_CLIENT = PNP + ".handler.client";
        static final public String PNP_CODEC = PNP + ".codec";
    }
}