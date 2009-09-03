package performanceTests.dataspace.local;

import org.objectweb.proactive.core.config.PAProperties;


public class TestIbis extends AbstractPAProviderLocalBenchmark {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("ibis");
    }

    public TestIbis() {
        super(TestIbis.class);
    }
}
