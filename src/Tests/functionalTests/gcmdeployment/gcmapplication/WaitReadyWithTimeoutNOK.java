package functionalTests.gcmdeployment.gcmapplication;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveTimeoutException;

import functionalTests.GCMFunctionalTest;


public class WaitReadyWithTimeoutNOK extends GCMFunctionalTest {

    public WaitReadyWithTimeoutNOK() {
        super(WaitReadyWithTimeoutNOK.class.getResource("gcma.xml"));
    }

    @Test(expected = ProActiveTimeoutException.class)
    public void test() throws ProActiveTimeoutException {
        super.gcmad.waitReady(50);
    }
}
