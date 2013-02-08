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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.amqp.federation;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.amqp.remoteobject.AbstractAMQPRemoteObject;
import org.objectweb.proactive.extensions.amqp.remoteobject.RpcReusableChannel;

import java.io.IOException;
import java.net.URI;


/**
 * Remote object client part for 'amqp-federation' protocol. It 
 * inherits RPC algorithm from AbstractAMQPRemoteObject and implements
 * parts specific for 'amqp-federation' protocol:
 * <ul>
 * <li>RpcReusableChannel is received using AMQPFederationUtils.getRpcChannel
 * (AMQPFederationUtils doesn't directly extracts broker's host/port from the remote 
 * object's URL, it uses brokers address mapping from the configuration)
 * <li>to check that target remote object's queue exists it executes it can't
 * use AMQP call 'queueDeclarePassive' since this queue can be created in
 * another broker, instead it sends special 'ping' message to the server object
 * </ul>
 * 
 * @author ProActive team
 * @since ProActive 5.2.0
 */
public class AMQPFederationRemoteObject extends AbstractAMQPRemoteObject {

    private static final String RPC_EXCHANGE_NAME = AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_EXCHANGE_NAME
            .getValue();

    private static final long RPC_REPLY_TIMEOUT = AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_TIMEOUT
            .getValue();

    public AMQPFederationRemoteObject(URI remoteObjectURL) throws ProActiveException, IOException {
        super(remoteObjectURL, RPC_EXCHANGE_NAME, RPC_REPLY_TIMEOUT);
    }

    @Override
    protected RpcReusableChannel getRpcReusableChannel() throws IOException {
        return AMQPFederationUtils.getRpcChannel(remoteObjectURL);
    }

    @Override
    protected void checkTargetObjectExists() throws IOException {
        if (!AMQPFederationUtils.pingRemoteObject(queueName, remoteObjectURL)) {
            throw new IOException("Failed to ping queue " + queueName);
        }
    }

}
