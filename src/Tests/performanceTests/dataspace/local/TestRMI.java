package performanceTests.dataspace.local;

import org.objectweb.proactive.core.config.PAProperties;


public class TestRMI extends AbstractPAProviderLocalBenchmark {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("rmi");
    }

    public TestRMI() {
        super(TestRMI.class);
    }
}
