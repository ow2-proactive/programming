package functionalTests.gcmdeployment.loadapplicationdescriptor;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;

import functionalTests.FunctionalTest;


public class TestLoadApplicationDescriptor extends FunctionalTest {

    @Test(expected = ProActiveException.class)
    public void test() throws ProActiveException {
        PAGCMDeployment.loadApplicationDescriptor(this.getClass().getResource("doesnotexist.xml"));
    }

    @Test(expected = ProActiveException.class)
    public void testNullURL() throws ProActiveException {
        PAGCMDeployment.loadApplicationDescriptor((URL) null);
    }

    @Test(expected = ProActiveException.class)
    public void testNullFile() throws ProActiveException {
        PAGCMDeployment.loadApplicationDescriptor((File) null);
    }

}
