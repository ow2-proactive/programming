package performanceTests.dataspace.remote;

import org.objectweb.proactive.core.config.PAProperties;


public class TestPAMR extends AbstractPAProviderRemoteBenchmark {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("pamr");
    }

    public TestPAMR() {
        super(TestPAMR.class);
    }
}
