package functionalTests.vm;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;

import functionalTests.FunctionalTest;


/**
 * just test the xml schema validation
 * @author jmguilla
 *
 */
public class TestVM extends FunctionalTest {

    @Test
    public void test() throws ProActiveException, IOException, InterruptedException {
        URL desc = this.getClass().getResource("hypervisorGCMA.xml");
        PAGCMDeployment.loadApplicationDescriptor(desc);
    }
}
