/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.messagerouting.message;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;
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
            InvocationTargetException, MalformedMessageException {
        for (int i = 0; i < NB_CHECK; i++) {
            buildAndCheckDataRequest(RegistrationRequestMessage.class, MessageType.REGISTRATION_REQUEST);
        }
    }

    /* Randomly construct valid registration reply then check all the fields.
     */
    @Test
    public void testRegistrationReply() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException, MalformedMessageException {
        for (int i = 0; i < NB_CHECK; i++) {
            buildAndCheckDataRequest(RegistrationReplyMessage.class, MessageType.REGISTRATION_REPLY);
        }
    }

    private void buildAndCheckDataRequest(Class<? extends RegistrationMessage> cl, MessageType type)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, InstantiationException,
            // can be thrown by RegistrationMessage(buf,0)
            MalformedMessageException {
        AgentID agent = new AgentID(ProActiveRandom.nextPosLong());
        logger.info("agent " + agent);
        long msgId = ProActiveRandom.nextPosLong();
        logger.info("msgId " + msgId);
        long routerId = ProActiveRandom.nextPosLong();
        Constructor<? extends RegistrationMessage> constructor;

        constructor = cl.getConstructor(AgentID.class, long.class, long.class);
        RegistrationMessage m = (RegistrationMessage) constructor.newInstance(agent, msgId, routerId);

        Assert.assertEquals(Message.PROTOV1, m.getProtoID());
        Assert.assertEquals(type, m.getType());
        Assert.assertEquals(msgId, m.getMessageID());
        Assert.assertEquals(agent, m.getAgentID());
        Assert.assertEquals(routerId, m.getRouterID());

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
