package functionalTests.gcmdeployment.executable;

import org.junit.Test;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderExecutable.Instances;


public class TestExecutableOnePerHost extends AbstractTestExecutable {

    public TestExecutableOnePerHost() {
        super(Instances.onePerHost);
    }

    @Test(timeout = 10000)
    public void Test() {
        while (1 != tmpDir.listFiles().length) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
