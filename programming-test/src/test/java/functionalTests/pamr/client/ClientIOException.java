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
package functionalTests.pamr.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.client.AgentImpl;
import org.objectweb.proactive.extensions.pamr.client.MessageHandler;
import org.objectweb.proactive.extensions.pamr.exceptions.PAMRException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.DebugMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.DebugMessage.DebugType;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.remoteobject.util.socketfactory.PAMRPlainSocketFactory;
import org.objectweb.proactive.utils.Sleeper;

import functionalTests.pamr.BlackBox;


@Ignore
public class ClientIOException extends BlackBox {
    int port;

    @Before
    public void before() throws IOException {
    }

    @Test
    public void test() throws UnknownHostException, ProActiveException, PAMRException {
        InetAddress localhost = InetAddress.getLocalHost();
        Agent agent = new Agent(localhost, super.router.getPort(), FakeMessageHandler.class);
        AgentID agentId = agent.getAgentID();

        Message message;

        message = new DebugMessage(agentId, 1, DebugType.DEB_NOOP);
        agent.sendMsg(message);
        message = new DebugMessage(agentId, 1, DebugType.DEB_DISCONNECT);
        agent.sendMsg(message);

        new Sleeper(1000, ProActiveLogger.getLogger(Loggers.SLEEPER)).sleep();
        message = new DebugMessage(agentId, 1, DebugType.DEB_NOOP);
        agent.sendMsg(message);

        new Sleeper(500000, ProActiveLogger.getLogger(Loggers.SLEEPER)).sleep();
        System.out.println("toto");
    }

    static public class FakeMessageHandler implements MessageHandler {

        public FakeMessageHandler(org.objectweb.proactive.extensions.pamr.client.Agent agentV2Internal) {
        }

        public void pushMessage(DataRequestMessage message) {
            // Mock
        }
    }

    static public class Agent extends AgentImpl {

        public Agent(InetAddress routerAddr, int routerPort, Class<? extends MessageHandler> messageHandlerClass)
                throws ProActiveException {
            super(routerAddr, routerPort, null, new MagicCookie(), messageHandlerClass, new PAMRPlainSocketFactory());
        }

        public void sendMsg(Message message) throws PAMRException {
            super.internalSendMsg(message);
        }

    }
}
