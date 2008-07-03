package functionalTests.gcmdeployment.executable;

import org.junit.Test;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderExecutable.Instances;


public class TestExecutableOnePerCapacity extends AbstractTExecutable {

    public TestExecutableOnePerCapacity() {
        super(Instances.onePerCapacity);
    }

    @Test(timeout = 10000)
    public void Test() {
        while (4 != tmpDir.listFiles().length) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
