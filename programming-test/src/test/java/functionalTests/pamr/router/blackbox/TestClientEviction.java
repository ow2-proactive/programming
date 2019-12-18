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
package functionalTests.pamr.router.blackbox;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.client.Tunnel;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extensions.pamr.router.Client;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;
import org.objectweb.proactive.extensions.pamr.router.RouterImpl;
import org.objectweb.proactive.utils.Sleeper;

import functionalTests.FunctionalTest;


/**
 * Check that client eviction mechanism works. Test scenario:
 * 
 *  1. Send registration message to the router, check that the client is added to the clientMap.
 *  
 *  (client is first disconnected with heartbeat mechanism, then evicted with eviction mechanism)
 *  
 *  2. Wait 2 * eviction timeout, check that the map is cleaned.
 */
public class TestClientEviction extends FunctionalTest {

    private static final long EVICTION_TIMEOUT = 3000;

    private static final int HEARTBEAT_TIMEOUT = 1000;

    protected RouterImpl router;

    protected Tunnel tunnel;

    @Before
    public void start() throws Exception {
        RouterConfig config = new RouterConfig();
        config.setHeartbeatTimeout(HEARTBEAT_TIMEOUT);
        config.setClientEvictionTimeout(EVICTION_TIMEOUT);
        this.router = (RouterImpl) Router.createAndStart(config);

        Socket s = new Socket(InetAddress.getLocalHost(), this.router.getPort());
        this.tunnel = new Tunnel(s);
    }

    @After
    public void stop() {
        this.tunnel.shutdown();
        this.router.stop();
    }

    @Test
    public void test() throws IOException, MalformedMessageException {
        MagicCookie magicCookie = new MagicCookie();
        Message message = new RegistrationRequestMessage(null,
                                                         ProActiveRandom.nextLong(),
                                                         RouterImpl.UNKNOWN_ROUTER_ID,
                                                         magicCookie);
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
        RegistrationReplyMessage reply = new RegistrationReplyMessage(resp, 0);
        AgentID firstID = reply.getAgentID();
        magicCookie = reply.getMagicCookie();

        new Sleeper(HEARTBEAT_TIMEOUT / 2, ProActiveLogger.getLogger(Loggers.SLEEPER)).sleep();

        Client client = router.getClient(firstID);
        Assert.assertNotNull("Client is not added to the map", client);

        new Sleeper(2 * EVICTION_TIMEOUT, ProActiveLogger.getLogger(Loggers.SLEEPER)).sleep();

        client = router.getClient(firstID);
        Assert.assertNull("Client is not evicted after timeout", client);

    }
}
