package performanceTests.throughput;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.NodeException;

import performanceTests.HudsonReport;
import functionalTests.GCMFunctionalTestDefaultNodes;


public abstract class Throughput extends GCMFunctionalTestDefaultNodes {

    private Class<?> cl;

    public Throughput(Class<?> cl) {
        super(1, 1);
        this.cl = cl;
    }

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {
        Server server = (Server) PAActiveObject.newActive(Server.class.getName(), new Object[] {}, super
                .getANode());
        Client client = (Client) PAActiveObject.newActive(Client.class.getName(), new Object[] { server });

        double throughput = client.runTest();
        HudsonReport.reportToHudson(this.cl, throughput);
    }

    @SuppressWarnings("serial")
    static public class Server implements Serializable {
        boolean firstRequest = true;
        long count = 0;
        long startTime;

        public Server() {

        }

        public void serve() {
            if (firstRequest) {
                startTime = System.currentTimeMillis();
                firstRequest = false;
            }

            count++;
        }

        public double finish() {
            long endTime = System.currentTimeMillis();
            double throughput = (1000.0 * count) / (endTime - startTime);

            System.out.println("Count: " + count);
            System.out.println("Duration: " + (endTime - startTime));
            System.out.println("Throughput " + throughput);
            return throughput;
        }
    }

    @SuppressWarnings("serial")
    static public class Client implements Serializable {
        private Server server;

        public Client() {

        }

        public Client(Server server) {
            this.server = server;
        }

        public double runTest() {
            // Warmup
            for (int i = 0; i < 1000; i++) {
                server.serve();
            }

            long startTime = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - startTime > PAProperties.PA_TEST_PERF_DURATION
                        .getValueAsInt())
                    break;

                for (int i = 0; i < 50; i++) {
                    server.serve();
                }
            }
            double throughput = server.finish();

            // startTest must be sync 
            return throughput;
        }
    }
}
