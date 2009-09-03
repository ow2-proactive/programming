package performanceTests.dataspace.remote;

import org.objectweb.proactive.core.config.PAProperties;


public class TestHTTP extends AbstractPAProviderRemoteBenchmark {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("http");
    }

    public TestHTTP() {
        super(TestHTTP.class);
    }
}
