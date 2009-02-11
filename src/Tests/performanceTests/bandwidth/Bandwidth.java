package performanceTests.bandwidth;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.NodeException;

import performanceTests.HudsonReport;
import functionalTests.GCMFunctionalTestDefaultNodes;


public abstract class Bandwidth extends GCMFunctionalTestDefaultNodes {
    /** The buffer included in each message */
    static final public byte buf[] = new byte[10 * 1024 * 1024]; // 1Mo

    private Class<?> cl;

    public Bandwidth(Class<?> cl) {
        super(1, 1);
        this.cl = cl;
    }

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {
        Server server = (Server) PAActiveObject.newActive(Server.class.getName(), new Object[] {}, super
                .getANode());
        Client client = (Client) PAActiveObject.newActive(Client.class.getName(), new Object[] { server });
        double bandwidth = client.runTest();
        HudsonReport.reportToHudson(this.cl, bandwidth);
    }

    @SuppressWarnings("serial")
    static public class Server implements Serializable {
        boolean firstRequest = true;
        long count = 0;
        long startTime;

        public Server() {

        }

        public int serve(byte[] buf) {
            if (firstRequest) {
                startTime = System.currentTimeMillis();
                firstRequest = false;
            }

            count++;
            return 0;
        }

        public double finish() {
            long endTime = System.currentTimeMillis();
            double size = (1.0 * Bandwidth.buf.length * count) / (1024 * 1024);

            System.out.println("Size: " + size);
            System.out.println("Duration: " + (endTime - startTime));

            double bandwith = (1000.0 * size) / (endTime - startTime);
            System.out.println("Bandwidth " + bandwith);
            return bandwith;
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
            for (int i = 0; i < 10; i++) {
                server.serve(TestRMI.buf);
            }
            System.out.println("End of warmup");

            long startTime = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - startTime > PAProperties.PA_TEST_PERF_DURATION
                        .getValueAsInt())
                    break;

                server.serve(TestRMI.buf);
            }
            double bandwidth = server.finish();

            // startTest must be sync
            return bandwidth;
        }
    }
}
