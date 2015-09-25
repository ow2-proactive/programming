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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.pamr.router.blackbox;

import java.io.IOException;

import org.junit.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage.ErrorType;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extensions.pamr.router.RouterImpl;

import functionalTests.pamr.BlackBox;


/**
 * Test how the router handles
 * corrupted registration messages
 */
public class TestRegistrationCorruption extends BlackBox {

    private static final long UNKNOWN_AGENT_ID = -1;
    private static final AgentID unknown = null;

    @Test
    public void test() throws IOException {
        long invalidAgentID;
        do {
            invalidAgentID = -ProActiveRandom.nextPosLong();
        } while (invalidAgentID == UNKNOWN_AGENT_ID);
        long invalidRouterID = -ProActiveRandom.nextPosLong();
        long msgId = ProActiveRandom.nextPosLong();

        MagicCookie magicCookie = new MagicCookie();
        Message message = new RegistrationRequestMessage(new AgentID(invalidAgentID), msgId,
            RouterImpl.DEFAULT_ROUTER_ID, magicCookie);
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
        // expect to get ERR_MALFORMED_MESSAGE
        ErrorMessage err = new ErrorMessage(resp, 0);
        Assert.assertEquals(ErrorType.ERR_MALFORMED_MESSAGE, err.getErrorType());
        Assert.assertEquals(msgId, err.getMessageID());
        // router has no additional information
        Assert.assertEquals(unknown, err.getRecipient());
        Assert.assertEquals(unknown, err.getFaulty());

        // Connect
        message = new RegistrationRequestMessage(null, msgId, RouterImpl.DEFAULT_ROUTER_ID, magicCookie);
        tunnel.write(message.toByteArray());

        resp = tunnel.readMessage();
        RegistrationReplyMessage reply = (RegistrationReplyMessage) Message.constructMessage(resp, 0);
        AgentID myAgentId = reply.getAgentID();

        message = new RegistrationRequestMessage(myAgentId, msgId, invalidRouterID, magicCookie);
        tunnel.write(message.toByteArray());

        resp = tunnel.readMessage();
        // expect to get ERR_MALFORMED_MESSAGE
        err = new ErrorMessage(resp, 0);
        Assert.assertEquals(ErrorType.ERR_MALFORMED_MESSAGE, err.getErrorType());
        Assert.assertEquals(msgId, err.getMessageID());
    }
}
