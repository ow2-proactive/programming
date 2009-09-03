package performanceTests.dataspace.local;

import org.objectweb.proactive.core.config.PAProperties;


public class TestPAMR extends AbstractPAProviderLocalBenchmark {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("pamr");
    }

    public TestPAMR() {
        super(TestPAMR.class);
    }
}
