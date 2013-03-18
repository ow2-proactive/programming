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
package functionalTests.pamr.router.blackbox;

import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.pamr.client.Agent;
import org.objectweb.proactive.extensions.pamr.client.AgentImpl;
import org.objectweb.proactive.extensions.pamr.client.MessageHandler;
import org.objectweb.proactive.extensions.pamr.exceptions.PAMRException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.remoteobject.util.socketfactory.PAMRPlainSocketFactory;

import functionalTests.pamr.BlackBox;


/**
 * Check that two agents can exchange data messages through the router. 
 */
public class TestAgentCommunication extends BlackBox {

    @Test
    public void test() throws UnknownHostException, ProActiveException, PAMRException {
        InetAddress localhost = InetAddress.getLocalHost();

        Agent replyingAgent = new SimpleAgent(localhost, super.router.getPort(), UpcasingHandler.class);
        AgentID replyingAgentId = replyingAgent.getAgentID();

        Agent agent = new SimpleAgent(localhost, super.router.getPort(), NOOPHandler.class);

        byte[] reply = agent.sendMsg(replyingAgentId, "Hello".getBytes(), false);
        Assert.assertEquals("HELLO", new String(reply));

        reply = agent.sendMsg(replyingAgentId, "Hello2".getBytes(), false);
        Assert.assertEquals("HELLO2", new String(reply));
    }

    static public class UpcasingHandler implements MessageHandler {

        private Agent agent;

        public UpcasingHandler(Agent agent) {
            this.agent = agent;
        }

        public void pushMessage(DataRequestMessage message) {
            byte[] data = message.getData();
            String replyStirng = new String(data).toUpperCase();
            try {
                agent.sendReply(message, replyStirng.getBytes());
            } catch (PAMRException e) {
                e.printStackTrace();
            }
        }
    }

    static public class NOOPHandler implements MessageHandler {

        public NOOPHandler(Agent agent) {
        }

        public void pushMessage(DataRequestMessage message) {
        }
    }

    static public class SimpleAgent extends AgentImpl {

        public SimpleAgent(InetAddress routerAddr, int routerPort,
                Class<? extends MessageHandler> messageHandlerClass) throws ProActiveException {
            super(routerAddr, routerPort, null, new MagicCookie(), messageHandlerClass,
                    new PAMRPlainSocketFactory());
        }

        public void sendMsg(Message message) throws PAMRException {
            super.internalSendMsg(message);
        }

    }

}
