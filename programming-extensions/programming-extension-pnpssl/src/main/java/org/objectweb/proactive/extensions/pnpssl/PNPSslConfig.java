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
package org.objectweb.proactive.extensions.pnpssl;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;
import org.objectweb.proactive.core.config.PAPropertyBoolean;
import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAPropertyString;
import org.objectweb.proactive.extensions.ssl.SslHelpers;


/**
 * The properties known by the PNP over SSL remote object factory.
 * <p>
 * Properties all the same than the one known by the standard PNP remote object factory plus
 * some extra ones.
 *
 * @since ProActive 5.0.0
 */
@PublicAPI
final public class PNPSslConfig implements PAPropertiesLoaderSPI {
    /**
     * The TCP port to bind to
     * <p>
     * PNP binds to a local TCP port to accept incoming connections. By default a free port is randomly chosen
     * (port 0). By setting this property, one can force the TCP port.
     */
    static final public PAPropertyInteger PA_PNPSSL_PORT = new PAPropertyInteger("proactive.pnps.port", false, 0);

    /**
     * The default heartbeat period (in milliseconds)
     * <p>
     * PNP offers an heartbeat mechanism to detect network failure. If set to 0 hearthbeats are disabled and
     * network failure will not be detected before the TCP timeout which can be quite long.
     * <p>
     * Setting this value too low impacts the network performance. It is not recommended to use a value
     * lower than 500 milliseconds.
     */
    static final public PAPropertyInteger PA_PNPSSL_DEFAULT_HEARTBEAT = new PAPropertyInteger("proactive.pnps.default_heartbeat",
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
    static final public PAPropertyInteger PA_PNPSSL_HEARTBEAT_FACTOR = new PAPropertyInteger("proactive.pnps.heartbeat_factor",
                                                                                             false,
                                                                                             3);

    static final public PAPropertyInteger PA_PNPSSL_HEARTBEAT_WINDOW = new PAPropertyInteger("proactive.pnps.heartbeat_window",
                                                                                             false,
                                                                                             5);

    /**
     * Channel garbage collection timeout (in milliseconds)
     * <p>
     * Idle channels are garbage collected after a given timeout. If a channel is unused since this value then
     * the channel will be garbage collected.
     */
    static final public PAPropertyInteger PA_PNPSSL_IDLE_TIMEOUT = new PAPropertyInteger("proactive.pnps.idle_timeout",
                                                                                         false,
                                                                                         60 * 1000);

    /**
     * The keystore to be used.
     * <p>
     * A keystore contains the private keys and the associated certificate to be used by the SSL layer.
     * The keystore must be in the PKCS12 format, and must contains at least one private keys. The keystore
     * and the keys must be protected by the password defined by {@link SslHelpers#DEFAULT_KS_PASSWD}.
     * <p>
     * If the keystore is not defined then a certificate is generated at runtime.
     */
    static final public PAPropertyString PA_PNPSSL_KEYSTORE = new PAPropertyString("proactive.pnps.keystore", false);

    /**
     * Should PNP over SSL authenticate remote peers ?
     * <p>
     * SSL can be used both for ciphering and authentication. By default PNP over SSL only ciphers communication
     * between remote runtimes. Anyone can connect to any runtime. By setting this property to true, authentication
     * is also performed. Remote runtimes must be able to offer a certificate which is on the local keystore defined
     * by {@link PNPSslConfig#PA_PNPSSL_KEYSTORE}
     */
    static final public PAPropertyBoolean PA_PNPSSL_AUTHENTICATE = new PAPropertyBoolean("proactive.pnps.authenticate",
                                                                                         false,
                                                                                         false);

    /**
     * The password associated to the keystore used for ensuring ciphering and authentication.
     */
    static public PAPropertyString PA_PNPSSL_KEYSTORE_PASSWORD = new PAPropertyString("proactive.pnps.keystore.password",
                                                                                      false,
                                                                                      "pkpass");

    public interface Loggers {
        static final public String PNPSSL = org.objectweb.proactive.core.util.log.Loggers.CORE + ".pnpssl";

        static final public String PNPSSL_HANDLER_SERVER = PNPSSL + ".handler.server";

        static final public String PNPSSL_HANDLER_CLIENT = PNPSSL + ".handler.client";

        static final public String PNPSSL_CODEC = PNPSSL + ".codec";
    }
}
