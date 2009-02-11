package functionalTests.messagerouting.router.blackbox;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.ErrorMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.ErrorMessage.ErrorType;

import functionalTests.messagerouting.BlackBox;


public class TestUnknownSender extends BlackBox {

    /* - Connect to the router
     * - Send a message to a non existent recipient with a bogus sender
     * - Do nothing since the router will drop the message
     */

    @Test
    public void testNOK() throws IOException, InstantiationException {
        AgentID srcAgentID = new AgentID(ProActiveRandom.nextPosLong());
        AgentID dstAgentID = new AgentID(ProActiveRandom.nextPosLong());
        long msgId = ProActiveRandom.nextPosLong();

        Message message = new DataRequestMessage(srcAgentID, dstAgentID, msgId, null);
        tunnel.write(message.toByteArray());

        // Since both src and dst agent are unknown, the router will drop this message
        // Their is no clean way to check that "no message has been send by the router"
        // and I don't want to wait for the timeout
    }

    /* - Connect to the router
     * - Send a message to a non existent recipient
     * - a ERR_UNKNOW_RCPT message is expected in response
     */
    @Test
    public void testOK() throws IOException, InstantiationException {
        // Connect
        Message message = new RegistrationRequestMessage(null, ProActiveRandom.nextPosLong());
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
        RegistrationReplyMessage reply = (RegistrationReplyMessage) Message.constructMessage(resp, 0);
        AgentID myAgentId = reply.getAgentID();

        // Send a message
        AgentID dstAgentID = new AgentID(ProActiveRandom.nextPosLong());
        long msgId = ProActiveRandom.nextPosLong();

        message = new DataRequestMessage(myAgentId, dstAgentID, msgId, null);
        tunnel.write(message.toByteArray());

        resp = tunnel.readMessage();
        ErrorMessage error = new ErrorMessage(resp, 0);
        Assert.assertEquals(error.getErrorType(), ErrorType.ERR_UNKNOW_RCPT);
        Assert.assertEquals(error.getMessageID(), msgId);
        Assert.assertEquals(error.getSender(), dstAgentID);
        Assert.assertEquals(error.getRecipient(), myAgentId);
    }
}
