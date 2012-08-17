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
import org.objectweb.proactive.core.config.PAPropertyString;


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
        "proactive.communication.amqp.broker.address", false, "localhost");

    /**
     * The port of the broker to use.
     * 5672 by default
     *
     */
    static public PAPropertyInteger PA_AMQP_BROKER_PORT = new PAPropertyInteger(
        "proactive.communication.amqp.broker.port", false, 5672);

    static public PAPropertyString PA_AMQP_QUEUE_PREFIX = new PAPropertyString(
        "proactive.communication.amqp.queue_prefix", false, "proactive.remoteobject.");

    static public PAPropertyString PA_AMQP_DISCOVERY_QUEUES_MESSAGE_TYPE = new PAPropertyString(
        "proactive.communication.amqp.discover_queues_message_type", false, "proactive.discover_queues");

    static final public PAPropertyString PA_AMQP_DISCOVER_EXCHANGE_NAME = new PAPropertyString(
        "proactive.communication.amqp.discover_exchange_name", false,
        "proactive.remoteobject.amqp_discover_exchange");

    static final public PAPropertyString PA_AMQP_RPC_EXCHANGE_NAME = new PAPropertyString(
        "proactive.communication.amqp.rpc_exchange_name", false, "proactive.remoteobject.amqp_rpc_exchange");

    public interface Loggers {

        // root logger for amqp
        static final public String AMQP = org.objectweb.proactive.core.util.log.Loggers.CORE + ".amqp";
        public static final String AMQP_REMOTE_OBJECT_FACTORY = AMQP + ".factory";
        public static final String AMQP_REMOTE_OBJECT = AMQP + ".remoteobject";
        public static final String AMQP_CHANNEL_FACTORY = AMQP + ".channelfactory";

    }
}
