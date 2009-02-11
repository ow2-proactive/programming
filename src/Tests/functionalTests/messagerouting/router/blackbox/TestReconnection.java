package functionalTests.messagerouting.router.blackbox;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationRequestMessage;

import functionalTests.messagerouting.BlackBox;


public class TestReconnection extends BlackBox {

    @Test
    public void test() throws IOException, InstantiationException {
        Message message = new RegistrationRequestMessage(null, ProActiveRandom.nextLong());
        tunnel.write(message.toByteArray());

        byte[] resp = tunnel.readMessage();
        RegistrationReplyMessage reply = new RegistrationReplyMessage(resp, 0);
        AgentID firstID = reply.getAgentID();

        // Ok it's time to reconnect

        message = new RegistrationRequestMessage(reply.getAgentID(), ProActiveRandom.nextLong());
        tunnel.write(message.toByteArray());

        resp = tunnel.readMessage();
        reply = new RegistrationReplyMessage(resp, 0);
        AgentID secondID = reply.getAgentID();

        Assert.assertEquals(firstID, secondID);

    }
}
