/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.messagerouting.router.blackbox;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.client.Tunnel;
import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.MagicCookie;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extra.messagerouting.router.RouterImpl;

import functionalTests.messagerouting.BlackBox;


public class TestReconnection extends BlackBox {

    @Test
    public void test() throws IOException, MalformedMessageException {
        MagicCookie magicCookie = new MagicCookie();
        Message message = new RegistrationRequestMessage(null, ProActiveRandom.nextLong(),
            RouterImpl.DEFAULT_ROUTER_ID, magicCookie);
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
        RegistrationReplyMessage reply = new RegistrationReplyMessage(resp, 0);
        AgentID firstID = reply.getAgentID();
        long routerID = reply.getRouterID();
        magicCookie = reply.getMagicCookie();

        // Ok it's time to reconnect

        this.tunnel.shutdown();
        Socket s = new Socket(InetAddress.getLocalHost(), this.router.getPort());
        this.tunnel = new Tunnel(s);

        message = new RegistrationRequestMessage(reply.getAgentID(), ProActiveRandom.nextLong(), routerID,
            magicCookie);
        tunnel.write(message.toByteArray());

        resp = tunnel.readMessage();
        reply = new RegistrationReplyMessage(resp, 0);
        AgentID secondID = reply.getAgentID();

        Assert.assertEquals(firstID, secondID);

    }
}
