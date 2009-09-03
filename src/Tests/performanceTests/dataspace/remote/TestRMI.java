package performanceTests.dataspace.remote;

import org.objectweb.proactive.core.config.PAProperties;


public class TestRMI extends AbstractPAProviderRemoteBenchmark {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("rmi");
    }

    public TestRMI() {
        super(TestRMI.class);
    }
}
