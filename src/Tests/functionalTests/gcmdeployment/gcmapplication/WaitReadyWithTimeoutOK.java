package functionalTests.gcmdeployment.gcmapplication;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveTimeoutException;

import functionalTests.GCMFunctionalTest;


public class WaitReadyWithTimeoutOK extends GCMFunctionalTest {

    public WaitReadyWithTimeoutOK() {
        super(WaitReadyWithTimeoutOK.class.getResource("gcma.xml"));
    }

    @Test
    public void testOK() throws ProActiveTimeoutException {
        super.gcmad.waitReady();
        super.gcmad.waitReady(50);
    }
}
