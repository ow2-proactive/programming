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
package org.objectweb.proactive.extensions.pnp;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;
import org.objectweb.proactive.core.config.PAPropertyInteger;


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
     * PNP offers an heartbeat mechanism to detect network failure. If set to 0 heartbeats are disabled and
     * network failure will not be detected before the TCP timeout which can be quite long.
     *
     * Setting this value too low impacts the network performance. It is not recommended to use a value
     * lower than 500 milliseconds.
     *
     */
    static final public PAPropertyInteger PA_PNP_DEFAULT_HEARTBEAT = new PAPropertyInteger("proactive.pnp.default_heartbeat",
                                                                                           false,
                                                                                           10 * 1000);

    /**
     * A factor of the heartbeat period used to detect failure.
     * The timeout used to detect a failure is set by default to heartbeat_period * factor.
     * <p>
     * This timeout is dynamically updated when receiving heartbeats to match the actual network delay observed.
     * It can never be decreased below the default value: heartbeat_period * factor.
     * <p>
     * When the delay between heartbeats increases due to network delay or machine load, then the corresponding timeout will also increase.
     * Technically, the last heartbeat interval is compared to proactive.pnp.heartbeat_window samples.
     * If the last interval is bigger than the average, then the timeout will be set to: last_interval * factor,
     * Otherwise, the timeout will be set to: average * factor
     * <p>
     * Thus, the heartbeat_factor property controls the tolerance of the mechanism regarding delay increase.
     * If the factor is set too low, it can imply that a slight delay will be acknowledged as a network failure.
     * If the factor is set too high, network failure detection will take a very long time.
     * <p>
     * The heartbeat_window property controls the "inertia" at which the timeout will be decreased back to its default value, after a period of increasing delay.
     * If the heartbeat_window is set too low, then only a few samples will be considered and the timeout will always be set according to the last heartbeat interval.
     * If the heartbeat_window is set too high, after a long period of network delay increase, the timeout will require the same amount of time to decrease back to its default value.
     */
    static final public PAPropertyInteger PA_PNP_HEARTBEAT_FACTOR = new PAPropertyInteger("proactive.pnp.heartbeat_factor",
                                                                                          false,
                                                                                          3);

    static final public PAPropertyInteger PA_PNP_HEARTBEAT_WINDOW = new PAPropertyInteger("proactive.pnp.heartbeat_window",
                                                                                          false,
                                                                                          5);

    /**
     * This property is only used for testing purpose, to simulate a server delay
     */
    static final public PAPropertyInteger PA_PNP_TEST_RANDOMDELAY = new PAPropertyInteger("proactive.pnp.test.random_delay",
                                                                                          false,
                                                                                          0);

    /**
     * Channel garbage collection timeout (in milliseconds)
     *
     * Idle channels are garbage collected after a given timeout. If a channel is unused since this value then
     * the channel will be garbage collected.
     */
    static final public PAPropertyInteger PA_PNP_IDLE_TIMEOUT = new PAPropertyInteger("proactive.pnp.idle_timeout",
                                                                                      false,
                                                                                      600 * 1000);

    private int port;

    private int idleTimeout;

    private int defaultHeartbeat;

    private int heartbeatFactor;

    private int heartbeatWindow;

    public PNPConfig() {
        this.port = PA_PNP_PORT.getDefaultValue();
        this.idleTimeout = PA_PNP_IDLE_TIMEOUT.getDefaultValue();
        this.defaultHeartbeat = PA_PNP_DEFAULT_HEARTBEAT.getDefaultValue();
        this.heartbeatFactor = PA_PNP_HEARTBEAT_FACTOR.getDefaultValue();
        this.heartbeatWindow = PA_PNP_HEARTBEAT_WINDOW.getDefaultValue();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public void setHeartbeatFactor(int heartbeatFactor) {
        this.heartbeatFactor = heartbeatFactor;
    }

    public int getHeartbeatFactor() {
        return heartbeatFactor;
    }

    public void setHeartbeatWindow(int heartbeatWindow) {
        this.heartbeatWindow = heartbeatWindow;
    }

    public int getHeartbeatWindow() {
        return heartbeatWindow;
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
