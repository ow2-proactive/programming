package functionalTests.messagerouting.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extra.messagerouting.client.AgentImpl;
import org.objectweb.proactive.extra.messagerouting.client.ProActiveMessageHandler;

import unitTests.UnitTests;


public class TestInvalidRouter extends UnitTests {

    @Test(expected = ProActiveException.class)
    public void test() throws ProActiveException, UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        new AgentImpl(localhost, 12423, ProActiveMessageHandler.class);
    }
}
