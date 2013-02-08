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
package org.objectweb.proactive.extensions.amqp.remoteobject;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;

import java.io.IOException;
import java.net.URI;


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
