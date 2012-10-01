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
package org.objectweb.proactive.extensions.amqp.federation;

import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;
import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAPropertyLong;
import org.objectweb.proactive.core.config.PAPropertyString;
import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPConstants;


public class AMQPFederationConfig implements PAPropertiesLoaderSPI {

    static public PAPropertyString PA_AMQP_FEDERATION_BROKER_ADDRESS = new PAPropertyString(
        "proactive.communication.amqp_federation.broker.address", false, AMQPConstants.DEFAULT_BROKER_HOST);

    static public PAPropertyInteger PA_AMQP_FEDERATION_BROKER_PORT = new PAPropertyInteger(
        "proactive.communication.amqp_federation.broker.port", false, AMQPConstants.DEFAULT_BROKER_PORT);

    static public PAPropertyString PA_AMQP_FEDERATION_BROKER_USER = new PAPropertyString(
        "proactive.communication.amqp_federation.broker.user", false, AMQPConstants.DEFAULT_USER);

    static public PAPropertyString PA_AMQP_FEDERATION_BROKER_PASSWORD = new PAPropertyString(
        "proactive.communication.amqp_federation.broker.password", false, AMQPConstants.DEFAULT_PASSWORD);

    static public PAPropertyString PA_AMQP_FEDERATION_BROKER_VHOST = new PAPropertyString(
        "proactive.communication.amqp_federation.broker.vhost", false, AMQPConstants.DEFAULT_VHOST);

    static final public PAPropertyString PA_AMQP_FEDERATION_DISCOVER_EXCHANGE_NAME = new PAPropertyString(
        "proactive.communication.amqp_federation.discover_exchange_name", false,
        "proactive.remoteobject.amqp_federation_discover_exchange");

    static final public PAPropertyString PA_AMQP_FEDERATION_RPC_EXCHANGE_NAME = new PAPropertyString(
        "proactive.communication.amqp_federation.rpc_exchange_name", false,
        "proactive.remoteobject.amqp_federation_rpc_exchange");

    static final public PAPropertyString PA_AMQP_FEDERATION_RPC_REPLY_EXCHANGE_NAME = new PAPropertyString(
        "proactive.communication.amqp_federation.rpc_reply_exchange_name", false,
        "proactive.remoteobject.amqp_federation_rpc_reply_exchange");

    static final public PAPropertyLong PA_AMQP_FEDERATION_RPC_TIMEOUT = new PAPropertyLong(
        "proactive.communication.amqp_federation.rpc_timeout", false, 10000);

    static final public PAPropertyLong PA_AMQP_FEDERATION_PING_TIMEOUT = new PAPropertyLong(
        "proactive.communication.amqp_federation.ping_timeout", false, 5000);

    static public PAPropertyString PA_AMQP_FEDERATION_BROKER_MAPPING_FILE = new PAPropertyString(
        "proactive.communication.amqp_federation.broker.mapping_file", false);

    static public PAPropertyString PA_AMQP_FEDERATION_SOCKET_FACTORY = new PAPropertyString(
        "proactive.communication.amqp_federation.socketfactory", false, "plain");

    /*
     * SSH tunnel parameters
     */

    static public PAPropertyString PA_AMQP_FEDERATION_SSH_KEY_DIR = new PAPropertyString(
        "proactive.amqp_federation.ssh.key_directory", false);

    static public PAPropertyString PA_AMQP_FEDERATION_SSH_KNOWN_HOSTS = new PAPropertyString(
        "proactive.amqp_federation.ssh.known_hosts", false);

    static public PAPropertyString PA_AMQP_FEDERATION_SSH_REMOTE_USERNAME = new PAPropertyString(
        "proactive.amqp_federation.ssh.username", false);

    static public PAPropertyInteger PA_AMQP_FEDERATION_SSH_REMOTE_PORT = new PAPropertyInteger(
        "proactive.amqp_federation.ssh.port", false);

}
