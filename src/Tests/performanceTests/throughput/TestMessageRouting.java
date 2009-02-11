package performanceTests.throughput;

import org.objectweb.proactive.core.config.PAProperties;


/**
 * Perfomance Test using MessageRouting protocol
 *
 */
public class TestMessageRouting extends Throughput {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("pamr");
        PAProperties.PA_NET_ROUTER_ADDRESS.setValue("localhost");
    }

    public TestMessageRouting() {
        super(TestMessageRouting.class);
    }
}
