package functionalTests.gcmdeployment.executable;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.xml.VariableContractType;

import functionalTests.GCMFunctionalTest;
import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestOptionalNodeProvider extends GCMFunctionalTest {

    String cookie = new Long(ProActiveRandom.nextLong()).toString();
    File tmpDir = new File(ProActiveConfiguration.getInstance().getProperty("java.io.tmpdir") +
        File.separator + this.getClass().getName() + cookie + File.separator);

    public TestOptionalNodeProvider() {
        super(AbstractTExecutable.class.getResource("TestOptionalNodeProvider.xml"));
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, "1",
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, "1",
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram("tmpDir", tmpDir.toString(),
                VariableContractType.DescriptorDefaultVariable);

        System.out.println("Temporary directory is: " + tmpDir.toString());
        Assert.assertTrue(tmpDir.mkdir());
    }

    @Test(timeout = 10000)
    public void test() {
        while (2 != tmpDir.listFiles().length) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void after() {
        for (File file : tmpDir.listFiles()) {
            file.delete();
        }
        tmpDir.delete();
    }

}
