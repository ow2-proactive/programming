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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.pamr;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.pamr.client.Agent;
import org.objectweb.proactive.extensions.pamr.client.AgentImpl;
import org.objectweb.proactive.extensions.pamr.client.MessageHandler;
import org.objectweb.proactive.extensions.pamr.exceptions.PAMRException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extensions.pamr.remoteobject.util.socketfactory.PAMRPlainSocketFactory;
import org.objectweb.proactive.extensions.pamr.router.Router;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.tools.jdi.SocketAttachingConnector;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


/**
 * Test that the heartbeat mechanism correctly handles the case where message reader thread in the agent dies.
 */
public class TestHeartbeat extends BlackBox {

    private static InetAddress localhost;
    private static int PORT = Integer.parseInt(System.getProperty("jdwp.port", "5550"));
    private static VirtualMachine virtualMachine;

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private static <T> T timedCall(long timeout, TimeUnit timeUnit, Callable<T> c)
            throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<T> task = new FutureTask<T>(c);
        THREAD_POOL.execute(task);
        return task.get(timeout, timeUnit);
    }

    @Test
    public void test() throws Exception {

        localhost = InetAddress.getLocalHost();

        Agent replyingAgent = createAgent(router, UpcasingHandler.class);
        final AgentID replyingAgentId = replyingAgent.getAgentID();

        final Agent agent = createAgent(router, NOOPHandler.class);

        byte[] reply = agent.sendMsg(replyingAgentId, "Hello".getBytes(), false);
        assertEquals("HELLO", new String(reply));

        suspendMessageReaderThread(replyingAgentId);

        try {
            timedCall(2 * config.getHeartbeatTimeout(), TimeUnit.MILLISECONDS, new Callable<Void>() {
                public Void call() throws Exception {
                    agent.sendMsg(replyingAgentId, "Hello2".getBytes(), false);
                    return null;
                }
            });
        } catch (TimeoutException e) {
            fail("sendMsg not interrupted by heartbeat mechanism");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            assertTrue("sendMsg should throw PAMRException when interrupted by heartbeat mechanism",
                    cause instanceof PAMRException);
            assertTrue(cause.getMessage().contains(
                    String.format("Remote agent %s disconnected", replyingAgentId.getId())));
        }

        resumeMessageReaderThread(replyingAgentId);
        // give it a chance to reconnect
        TimeUnit.MICROSECONDS.sleep(2 * config.getHeartbeatTimeout());

        reply = agent.sendMsg(replyingAgentId, "Hello2".getBytes(), false);
        assertEquals("HELLO2", new String(reply));
    }

    @BeforeClass
    public static void connect() throws Exception {
        SocketAttachingConnector socketConnector = null;
        for (Connector connector : Bootstrap.virtualMachineManager().allConnectors()) {
            if (connector instanceof SocketAttachingConnector) {
                socketConnector = (SocketAttachingConnector) connector;
            }
        }
        if (socketConnector == null) {
            throw new RuntimeException("Failed to find SocketAttachingConnector");
        }

        Map<String, ? extends Connector.Argument> args = socketConnector.defaultArguments();
        Connector.IntegerArgument port = (Connector.IntegerArgument) args.get("port");
        port.setValue(PORT);

        Connector.StringArgument hostname = (Connector.StringArgument) args.get("hostname");
        hostname.setValue("localhost");

        virtualMachine = socketConnector.attach(args);
    }

    private void suspendMessageReaderThread(AgentID agentId) throws Exception {
        ThreadReference tr = getMessageReaderThread(agentId);
        System.out.println("Suspending " + tr.name());
        tr.suspend();
    }

    private void resumeMessageReaderThread(AgentID agentId) throws Exception {
        ThreadReference tr = getMessageReaderThread(agentId);
        System.out.println("Resuming " + tr.name());
        tr.resume();
    }

    private ThreadReference getMessageReaderThread(AgentID agentId) throws Exception {
        String threadName = String.format("Message routing: message reader for agent %s", agentId.getId());
        List<ThreadReference> threads = virtualMachine.allThreads();
        for (ThreadReference tr : threads) {
            if (tr.name().contains(threadName)) {
                return tr;
            }
        }
        return null;
    }

    static public class UpcasingHandler implements MessageHandler {

        private Agent agent;

        public UpcasingHandler(Agent agent) {
            this.agent = agent;
        }

        public void pushMessage(DataRequestMessage message) {
            byte[] data = message.getData();
            String replyStirng = new String(data).toUpperCase();
            try {
                agent.sendReply(message, replyStirng.getBytes());
            } catch (PAMRException e) {
                e.printStackTrace();
            }
        }
    }

    static public class NOOPHandler implements MessageHandler {

        public NOOPHandler(Agent agent) {
        }

        public void pushMessage(DataRequestMessage message) {
        }
    }

    static private Agent createAgent(Router router, Class<? extends MessageHandler> messageHandlerClass)
            throws IllegalArgumentException, ProActiveException {
        return new AgentImpl(localhost, router.getPort(), null, new MagicCookie(), messageHandlerClass,
            new PAMRPlainSocketFactory());

    }

}
