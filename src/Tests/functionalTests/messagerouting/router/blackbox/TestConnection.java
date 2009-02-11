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
