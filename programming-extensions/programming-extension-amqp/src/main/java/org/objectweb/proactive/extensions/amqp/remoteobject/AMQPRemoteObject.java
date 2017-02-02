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
package org.objectweb.proactive.extensions.amqp.remoteobject;

import java.io.IOException;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;


/**
 * Remote object client part for 'amqp' protocol. It 
 * inherits RPC algorithm from AbstractAMQPRemoteObject and implements
 * parts specific for 'amqp' protocol:
 * <ul>
 * <li>RpcReusableChannel is received using AMQPUtils.getRpcChannel
 * (to connect to the broker AMQPUtils extracts broker's host/port from the remote object's URL)
 * <li>to check that target remote object's queue exists it executes
 * special AMQP call 'queueDeclarePassive'
 * </ul>
 * 
 * @author ProActive team
 * @since ProActive 5.2.0
 */
public class AMQPRemoteObject extends AbstractAMQPRemoteObject {

    private static final String RPC_EXCHANGE_NAME = AMQPConfig.PA_AMQP_RPC_EXCHANGE_NAME.getValue();

    private static final long RPC_REPLY_TIMEOUT = AMQPConfig.PA_AMQP_RPC_TIMEOUT.getValue();

    public AMQPRemoteObject(URI remoteObjectURL) throws ProActiveException, IOException {
        super(remoteObjectURL, RPC_EXCHANGE_NAME, RPC_REPLY_TIMEOUT);
    }

    @Override
    protected RpcReusableChannel getRpcReusableChannel() throws IOException {
        return AMQPUtils.getRpcChannel(remoteObjectURL);
    }

    @Override
    protected void checkTargetObjectExists() throws IOException {
        ReusableChannel queueCheckChannel = AMQPUtils.getChannel(remoteObjectURL);
        try {
            queueCheckChannel.getChannel().queueDeclarePassive(queueName);
            queueCheckChannel.returnChannel();
        } catch (IOException e) {
            // according to the AMQP spec channel is closed after 'queueDeclarePassive' fails 
            throw new IOException("Failed to get response while sending request to the " + queueName, e);
        }
    }

}
