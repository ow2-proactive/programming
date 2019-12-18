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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extensions.pamr.router.RouterImpl;

import functionalTests.pamr.BlackBox;


public class TestConnection extends BlackBox {

    /*
     * Send a valid Registration Request without agent ID
     *
     * A registration reply is expected with the same AgentID is expected
     */
    @Test
    public void testConnection() throws IOException, MalformedMessageException {
        MagicCookie magicCookie = new MagicCookie();
        Message message = new RegistrationRequestMessage(null,
                                                         ProActiveRandom.nextPosLong(),
                                                         RouterImpl.UNKNOWN_ROUTER_ID,
                                                         magicCookie);
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
        RegistrationReplyMessage reply = (RegistrationReplyMessage) Message.constructMessage(resp, 0);

        Assert.assertEquals(message.getMessageID(), reply.getMessageID());
        Assert.assertNotNull(reply.getAgentID());
        Assert.assertTrue(reply.getAgentID().getId() >= 0);
        Assert.assertEquals(magicCookie, reply.getMagicCookie());
    }

    /*
     * Send a valid Registration Request without agent ID and with a bad router ID
     *
     * A registration reply is expected with the same AgentID is expected
     */
    @Ignore
    @Test(expected = IOException.class)
    public void testInvalidConnection() throws IOException {
        Message message = new RegistrationRequestMessage(null,
                                                         ProActiveRandom.nextPosLong(),
                                                         0xbadbad,
                                                         new MagicCookie());
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
    }

}
