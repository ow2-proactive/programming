package functionalTests.messagerouting.message;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;

import unitTests.UnitTests;


public class TestMessageRegistration extends UnitTests {

    static final int NB_CHECK = 100;

    /* Randomly construct valid registration request then check all the fields.
     */
    @Test
    public void testRegistrationRequest() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        for (int i = 0; i < NB_CHECK; i++) {
            buildAndCheckDataRequest(RegistrationRequestMessage.class, MessageType.REGISTRATION_REQUEST);
        }
    }

    /* Randomly construct valid registration reply then check all the fields.
     */
    @Test
    public void testRegistrationReply() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        for (int i = 0; i < NB_CHECK; i++) {
            buildAndCheckDataRequest(RegistrationReplyMessage.class, MessageType.REGISTRATION_REPLY);
        }
    }

    private void buildAndCheckDataRequest(Class<? extends RegistrationMessage> cl, MessageType type)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        AgentID agent = new AgentID(ProActiveRandom.nextPosLong());
        logger.info("agent " + agent);
        long msgId = ProActiveRandom.nextPosLong();
        logger.info("msgId " + msgId);

        Constructor<? extends RegistrationMessage> constructor;
        constructor = cl.getConstructor(AgentID.class, long.class);
        RegistrationMessage m = (RegistrationMessage) constructor.newInstance(agent, msgId);

        Assert.assertEquals(Message.PROTOV1, m.getProtoID());
        Assert.assertEquals(type, m.getType());
        Assert.assertEquals(msgId, m.getMessageID());
        Assert.assertEquals(agent, m.getAgentID());

        byte[] buf = m.toByteArray();
        Assert.assertEquals(buf.length, m.getLength());

        constructor = cl.getConstructor(byte[].class, int.class);
        RegistrationMessage m2 = (RegistrationMessage) constructor.newInstance(buf, 0);

        Assert.assertEquals(m.getLength(), m2.getLength());
        Assert.assertEquals(m.getProtoID(), m2.getProtoID());
        Assert.assertEquals(m.getType(), m2.getType());
        Assert.assertEquals(m.getMessageID(), m2.getMessageID());
        Assert.assertEquals(m.getAgentID(), m2.getAgentID());
    }
}
