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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
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
        Message message = new RegistrationRequestMessage(null, ProActiveRandom.nextLong(),
            RouterImpl.DEFAULT_ROUTER_ID, magicCookie);
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
        RegistrationReplyMessage reply = new RegistrationReplyMessage(resp, 0);
        AgentID firstID = reply.getAgentID();
        magicCookie = reply.getMagicCookie();

        new Sleeper(HEARTBEAT_TIMEOUT / 2).sleep();

        Client client = router.getClient(firstID);
        Assert.assertNotNull("Client is not added to the map", client);

        new Sleeper(2 * EVICTION_TIMEOUT).sleep();

        client = router.getClient(firstID);
        Assert.assertNull("Client is not evicted after timeout", client);

    }
}
