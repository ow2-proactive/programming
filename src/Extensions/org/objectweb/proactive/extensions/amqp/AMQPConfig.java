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
package org.objectweb.proactive.extensions.amqp;

import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;
import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAPropertyLong;
import org.objectweb.proactive.core.config.PAPropertyString;
import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPConstants;


/**
 *  AMQP properties
 * @since ProActive 5.2.0
 */

public class AMQPConfig implements PAPropertiesLoaderSPI {

    /**
     * The address of the broker to use. localhost by default
     *
     * Can be FQDN or an IP address
     */
    static public PAPropertyString PA_AMQP_BROKER_ADDRESS = new PAPropertyString(
        "proactive.communication.amqp.broker.address", false, AMQPConstants.DEFAULT_BROKER_HOST);

    /**
     * The port of the broker to use.
     * 5672 by default
     *
     */
    static public PAPropertyInteger PA_AMQP_BROKER_PORT = new PAPropertyInteger(
        "proactive.communication.amqp.broker.port", false, AMQPConstants.DEFAULT_BROKER_PORT);

    static public PAPropertyString PA_AMQP_BROKER_USER = new PAPropertyString(
        "proactive.communication.amqp.broker.user", false, AMQPConstants.DEFAULT_USER);

    static public PAPropertyString PA_AMQP_BROKER_PASSWORD = new PAPropertyString(
        "proactive.communication.amqp.broker.password", false, AMQPConstants.DEFAULT_PASSWORD);

    static public PAPropertyString PA_AMQP_BROKER_VHOST = new PAPropertyString(
        "proactive.communication.amqp.broker.vhost", false, AMQPConstants.DEFAULT_VHOST);

    static final public PAPropertyString PA_AMQP_DISCOVER_EXCHANGE_NAME = new PAPropertyString(
        "proactive.communication.amqp.discover_exchange_name", false,
        "proactive.remoteobject.amqp_discover_exchange");

    static final public PAPropertyString PA_AMQP_RPC_EXCHANGE_NAME = new PAPropertyString(
        "proactive.communication.amqp.rpc_exchange_name", false, "proactive.remoteobject.amqp_rpc_exchange");

    static final public PAPropertyLong PA_AMQP_RPC_TIMEOUT = new PAPropertyLong(
        "proactive.communication.amqp.rpc_timeout", false, 10000);

    static public PAPropertyString PA_AMQP_SOCKET_FACTORY = new PAPropertyString(
        "proactive.communication.amqp.socketfactory", false, "plain");

    /*
     * SSH tunnel parameters
     */

    static public PAPropertyString PA_AMQP_SSH_KEY_DIR = new PAPropertyString(
        "proactive.amqp.ssh.key_directory", false);

    static public PAPropertyString PA_AMQP_SSH_KNOWN_HOSTS = new PAPropertyString(
        "proactive.amqp.ssh.known_hosts", false);

    static public PAPropertyString PA_AMQP_SSH_REMOTE_USERNAME = new PAPropertyString(
        "proactive.amqp.ssh.username", false);

    static public PAPropertyInteger PA_AMQP_SSH_REMOTE_PORT = new PAPropertyInteger(
        "proactive.amqp.ssh.port", false);

    public interface Loggers {

        // root logger for amqp
        static final public String AMQP = org.objectweb.proactive.core.util.log.Loggers.CORE + ".amqp";
        public static final String AMQP_REMOTE_OBJECT_FACTORY = AMQP + ".factory";
        public static final String AMQP_REMOTE_OBJECT = AMQP + ".remoteobject";
        public static final String AMQP_CHANNEL_FACTORY = AMQP + ".channelfactory";

    }
}
