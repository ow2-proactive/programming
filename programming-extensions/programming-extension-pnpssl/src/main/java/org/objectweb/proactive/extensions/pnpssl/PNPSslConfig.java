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
package org.objectweb.proactive.extensions.pnpssl;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAPropertyBoolean;
import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;
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
    static final public PAPropertyInteger PA_PNPSSL_PORT = new PAPropertyInteger("proactive.pnps.port",
        false, 0);

    /**
     * The default heartbeat period (in milliseconds)
     * <p>
     * PNP offers an heartbeat mechanism to detect network failure. If set to 0 hearthbeats are disabled and
     * network failure will not be detected before the TCP timeout which can be quite long.
     * <p>
     * Setting this value too low impacts the network performance. It is not recommended to use a value
     * lower than 500 milliseconds.
     */
    static final public PAPropertyInteger PA_PNPSSL_DEFAULT_HEARTBEAT = new PAPropertyInteger(
        "proactive.pnps.default_heartbeat", false, 9000);

    /**
     * Channel garbage collection timeout (in milliseconds)
     * <p>
     * Idle channels are garbage collected after a given timeout. If a channel is unused since this value then
     * the channel will be garbage collected.
     */
    static final public PAPropertyInteger PA_PNPSSL_IDLE_TIMEOUT = new PAPropertyInteger(
        "proactive.pnps.idle_timeout", false, 60 * 1000);

    /**
     * The keystore to be used.
     * <p>
     * A keystore contains the private keys and the associated certificate to be used by the SSL layer.
     * The keystore must be in the PKCS12 format, and must contains at least one private keys. The keystore
     * and the keys must be protected by the password defined by {@link SslHelpers#DEFAULT_KS_PASSWD}.
     * <p>
     * If the keystore is not defined then a certificate is generated at runtime.
     */
    static final public PAPropertyString PA_PNPSSL_KEYSTORE = new PAPropertyString("proactive.pnps.keystore",
        false);

    /**
     * Should PNP over SSL authenticate remote peers ?
     * <p>
     * SSL can be used both for ciphering and authentication. By default PNP over SSL only ciphers communication
     * between remote runtimes. Anyone can connect to any runtime. By setting this property to true, authentication
     * is also performed. Remote runtimes must be able to offer a certificate which is on the local keystore defined
     * by {@link PNPSslConfig#PA_PNPSSL_KEYSTORE}
     */
    static final public PAPropertyBoolean PA_PNPSSL_AUTHENTICATE = new PAPropertyBoolean(
        "proactive.pnps.authenticate", false, false);

    /**
     * The password associated to the keystore used for ensuring ciphering and authentication.
     */
    static public PAPropertyString PA_PNPSSL_KEYSTORE_PASSWORD = new PAPropertyString(
        "proactive.pnps.keystore.password", false, "pkpass");

    public interface Loggers {
        static final public String PNPSSL = org.objectweb.proactive.core.util.log.Loggers.CORE + ".pnpssl";
        static final public String PNPSSL_HANDLER_SERVER = PNPSSL + ".handler.server";
        static final public String PNPSSL_HANDLER_CLIENT = PNPSSL + ".handler.client";
        static final public String PNPSSL_CODEC = PNPSSL + ".codec";
    }
}