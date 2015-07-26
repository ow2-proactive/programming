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
import org.objectweb.proactive.extensions.pamr.protocol.message.DataReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage.ErrorType;

import functionalTests.pamr.BlackBoxRegistered;


/**
* Test how the router handles
 * corrupted data messages
 */
public class TestDataCorruption extends BlackBoxRegistered {

    @Test
    public void testRequest() throws IOException {
        long invalidID = -ProActiveRandom.nextPosLong();
        AgentID invalidAgentID = new AgentID(invalidID);
        long msgId = ProActiveRandom.nextPosLong();

        DataRequestMessage msg = new DataRequestMessage(this.agentId, invalidAgentID, msgId, null);
        tunnel.write(msg.toByteArray());

        byte[] resp = tunnel.readMessage();
        // expect to get ERR_MALFORMED_MESSAGE
        ErrorMessage err = new ErrorMessage(resp, 0);
        Assert.assertEquals(ErrorType.ERR_MALFORMED_MESSAGE, err.getErrorType());
        Assert.assertEquals(msgId, err.getMessageID());
        // router knows the recipient only
        Assert.assertEquals(null, err.getFaulty());
        Assert.assertEquals(this.agentId, err.getRecipient());

        msg = new DataRequestMessage(invalidAgentID, this.agentId, msgId, null);
        tunnel.write(msg.toByteArray());

        resp = tunnel.readMessage();
        // expect to get ERR_MALFORMED_MESSAGE
        err = new ErrorMessage(resp, 0);
        Assert.assertEquals(ErrorType.ERR_MALFORMED_MESSAGE, err.getErrorType());
        Assert.assertEquals(msgId, err.getMessageID());
        // router knows the faulty agent
        Assert.assertEquals(this.agentId, err.getFaulty());
        Assert.assertEquals(null, err.getRecipient());
    }

    @Test
    public void testReply() throws IOException {
        long invalidID = -ProActiveRandom.nextPosLong();
        AgentID invalidAgentID = new AgentID(invalidID);
        long msgId = ProActiveRandom.nextPosLong();

        DataReplyMessage msg = new DataReplyMessage(this.agentId, invalidAgentID, msgId, null);
        tunnel.write(msg.toByteArray());

        byte[] resp = tunnel.readMessage();
        // expect to get ERR_MALFORMED_MESSAGE
        ErrorMessage err = new ErrorMessage(resp, 0);
        Assert.assertEquals(ErrorType.ERR_MALFORMED_MESSAGE, err.getErrorType());
        Assert.assertEquals(msgId, err.getMessageID());
        // router knows the recipient only
        Assert.assertEquals(null, err.getFaulty());
        Assert.assertEquals(this.agentId, err.getRecipient());

        msg = new DataReplyMessage(invalidAgentID, this.agentId, msgId, null);
        tunnel.write(msg.toByteArray());

        resp = tunnel.readMessage();
        // expect to get ERR_MALFORMED_MESSAGE
        err = new ErrorMessage(resp, 0);
        Assert.assertEquals(ErrorType.ERR_MALFORMED_MESSAGE, err.getErrorType());
        Assert.assertEquals(msgId, err.getMessageID());
        // router knows the faulty agent
        Assert.assertEquals(this.agentId, err.getFaulty());
        Assert.assertEquals(null, err.getRecipient());
    }

}
