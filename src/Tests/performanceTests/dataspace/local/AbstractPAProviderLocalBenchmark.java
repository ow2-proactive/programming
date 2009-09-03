package performanceTests.dataspace.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.TimeoutAccounter;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;

import functionalTests.FunctionalTest;


public abstract class AbstractPAProviderLocalBenchmark extends FunctionalTest {
    final Class<? extends AbstractPAProviderLocalBenchmark> cl;

    static {
        System.setProperty("proactive.test.timeout", "600000");
    }

    FileSystemServerDeployer fsDeployer;
    NamingServiceDeployer namingServiceDeployer;

    public AbstractPAProviderLocalBenchmark(Class<? extends AbstractPAProviderLocalBenchmark> cl) {
        this.cl = cl;
    }

    @Before
    public void before() throws IOException, ProActiveException, URISyntaxException {
        this.startDataSpace();
    }

    @Test
    public void test() throws SpaceNotFoundException, NotConfiguredException, ConfigurationException,
            IOException {
        final DataSpacesFileObject fo = PADataSpaces.resolveDefaultInput("/zero");
        final InputStream is = fo.getContent().getInputStream();

        for (int bs = 1; bs < 1 << 25; bs <<= 1) {

            long before = System.currentTimeMillis();

            long count = 0;
            byte[] buf = new byte[bs];
            TimeoutAccounter ta = TimeoutAccounter.getAccounter(10000);
            while (!ta.isTimeoutElapsed()) {
                is.read(buf);
                count++;
            }

            final long tx = bs * count;
            final long after = System.currentTimeMillis();
            final double bw = ((tx * 1000.0) / (after - before)) / (1 << 20);
            System.out.printf(
                    "Block size: %8d bytes, bandwidth: %8.2f MiB/s, TX: %5d MiB, Time elapsed: %d ms\n", bs,
                    bw, (tx / (1 << 20)), (after - before));
        }

        is.close();
    }

    @After
    public void after() throws ProActiveException {
        this.stopDataSpace();
    }

    private void startDataSpace() throws IOException, ProActiveException, URISyntaxException {
        fsDeployer = new FileSystemServerDeployer("/dev/", true);
        namingServiceDeployer = new NamingServiceDeployer();

        final long applicationId = 0xcafe;
        namingServiceDeployer.getLocalNamingService().registerApplication(applicationId, null);
        final Node halfBodiesNode = NodeFactory.getHalfBodiesNode();

        // node is configured without scratch
        DataSpacesNodes.configureNode(halfBodiesNode, null);
        DataSpacesNodes.configureApplication(halfBodiesNode, applicationId, namingServiceDeployer
                .getNamingServiceURL());

        PADataSpaces.addDefaultInput(fsDeployer.getVFSRootURL(), null);
    }

    private void stopDataSpace() throws ProActiveException {
        // after using DS, we can clean up
        DataSpacesNodes.closeNodeConfig(NodeFactory.getHalfBodiesNode());
        namingServiceDeployer.terminate();
        fsDeployer.terminate();
    }
}
