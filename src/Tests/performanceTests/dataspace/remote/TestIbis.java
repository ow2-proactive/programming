package performanceTests.dataspace.remote;

import org.objectweb.proactive.core.config.PAProperties;


public class TestIbis extends AbstractPAProviderRemoteBenchmark {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("ibis");
    }

    public TestIbis() {
        super(TestIbis.class);
    }
}
