package functionalTests.gcmdeployment.executable;

import org.junit.Test;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderExecutable.Instances;


public class TestExecutableOnePerVM extends AbstractTExecutable {

    public TestExecutableOnePerVM() {
        super(Instances.onePerVM);
    }

    @Test(timeout = 10000)
    public void Test() {
        while (2 != tmpDir.listFiles().length) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
