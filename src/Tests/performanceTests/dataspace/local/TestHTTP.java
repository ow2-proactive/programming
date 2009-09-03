package performanceTests.dataspace.local;

import org.objectweb.proactive.core.config.PAProperties;


public class TestHTTP extends AbstractPAProviderLocalBenchmark {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("http");
    }

    public TestHTTP() {
        super(TestHTTP.class);
    }
}
