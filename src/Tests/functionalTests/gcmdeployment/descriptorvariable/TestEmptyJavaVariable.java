package functionalTests.gcmdeployment.descriptorvariable;

import java.net.URL;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;

import functionalTests.FunctionalTest;


/**
 * @BugID PROACTIVE-363
 */
public class TestEmptyJavaVariable extends FunctionalTest {
    final static String xmlFile = TestEmptyJavaVariable.class.getName() + ".xml";

    @Test(expected = ProActiveException.class)
    public void test() throws ProActiveException {
        URL url = this.getClass().getResource(xmlFile);
        PAGCMDeployment.loadApplicationDescriptor(url);
    }
}
