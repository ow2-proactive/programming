package functionalTests.messagerouting.message;

import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationRequestMessage;

import unitTests.UnitTests;


public class TestMessageInvalidConstructor extends UnitTests {
    /*
     *  NOTE: Does not test all the combinations
     *  
     */

    /* Data reply -> byte [] -> Data request must throw an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void test1() throws InstantiationException {
        AgentID srcAgent = new AgentID(ProActiveRandom.nextPosLong());
        AgentID dstAgent = new AgentID(ProActiveRandom.nextPosLong());
        long msgId = ProActiveRandom.nextPosLong();

        DataReplyMessage rp = new DataReplyMessage(srcAgent, dstAgent, msgId, null);
        new DataRequestMessage(rp.toByteArray(), 0);
    }

    /* Data reply -> byte [] -> Registration reply must throw an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void test2() throws InstantiationException {
        AgentID srcAgent = new AgentID(ProActiveRandom.nextPosLong());
        AgentID dstAgent = new AgentID(ProActiveRandom.nextPosLong());
        long msgId = ProActiveRandom.nextPosLong();

        DataReplyMessage rp = new DataReplyMessage(srcAgent, dstAgent, msgId, null);
        new RegistrationReplyMessage(rp.toByteArray(), 0);
    }

    /* Data reply -> byte [] -> Registration request must throw an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void test3() throws InstantiationException {
        AgentID srcAgent = new AgentID(ProActiveRandom.nextPosLong());
        AgentID dstAgent = new AgentID(ProActiveRandom.nextPosLong());
        long msgId = ProActiveRandom.nextPosLong();

        DataReplyMessage rp = new DataReplyMessage(srcAgent, dstAgent, msgId, null);
        new RegistrationRequestMessage(rp.toByteArray(), 0);
    }

}
