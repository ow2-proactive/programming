package functionalTests.gcmdeployment.executable;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderExecutable.Instances;

import functionalTests.GCMFunctionalTest;
import functionalTests.GCMFunctionalTestDefaultNodes;


public class AbstractTExecutable extends GCMFunctionalTest {

    String cookie = new Long(ProActiveRandom.nextLong()).toString();
    File tmpDir = new File(ProActiveConfiguration.getInstance().getProperty("java.io.tmpdir") +
        File.separator + this.getClass().getName() + cookie + File.separator);

    public AbstractTExecutable(Instances instances) {
        super(AbstractTExecutable.class.getResource("TestExecutable.xml"));
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, "2",
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, "2",
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram("tmpDir", tmpDir.toString(),
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram("instances", instances.toString(),
                VariableContractType.DescriptorDefaultVariable);

        System.out.println("Temporary directory is: " + tmpDir.toString());
        Assert.assertTrue(tmpDir.mkdir());
    }

    @After
    public void after() {
        for (File file : tmpDir.listFiles()) {
            file.delete();
        }
        tmpDir.delete();
    }

}
