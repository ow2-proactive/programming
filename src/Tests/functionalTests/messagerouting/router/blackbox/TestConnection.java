/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.messagerouting.router.blackbox;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationRequestMessage;

import functionalTests.messagerouting.BlackBox;


public class TestConnection extends BlackBox {

    /*
     * Send a valid Registration Request without agent ID
     *
     * A registration reply is expected with the same AgentID is expected
     */
    @Test
    public void testConnection() throws IOException {
        Message message = new RegistrationRequestMessage(null, ProActiveRandom.nextPosLong());
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
        RegistrationReplyMessage reply = (RegistrationReplyMessage) Message.constructMessage(resp, 0);

        Assert.assertEquals(message.getMessageID(), reply.getMessageID());
        Assert.assertNotNull(reply.getAgentID());
        Assert.assertTrue(reply.getAgentID().getId() >= 0);
    }
}
