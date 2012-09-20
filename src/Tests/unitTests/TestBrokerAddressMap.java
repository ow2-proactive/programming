package unitTests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.amqp.federation.BrokerAddressMap;
import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPConnectionParameters;
import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPConstants;


/**
 * Test against parsing logic implemented in the BrokerAddressMap.createFromMappingFile. 
 */
public class TestBrokerAddressMap {

    static File tmpFile;

    @BeforeClass
    public static void createTmpFile() throws IOException {
        tmpFile = File.createTempFile("test", "tmp");
    }

    @AfterClass
    public static void deleteTmpFile() {
        if (tmpFile != null) {
            tmpFile.delete();
        }
    }

    private BrokerAddressMap createForConfig(AMQPConnectionParameters defaultParams, String config)
            throws IOException {
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(config);
        writer.close();
        return BrokerAddressMap.createFromMappingFile(defaultParams, tmpFile.getAbsolutePath());
    }

    @Test
    public void test() throws Exception {
        AMQPConnectionParameters defaultParams = new AMQPConnectionParameters("defaultHost", 1,
            "defaultUser", "defaultPassword", "defaultVhost");

        try {
            BrokerAddressMap.createFromMappingFile(defaultParams, "invalid_file_name");
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        BrokerAddressMap addressMap;
        String config;

        // config with one broker, all parameters are set
        config = "broker_key.broker1=host1:123\n" + "broker1.host=host2\n" + "broker1.port=456\n"
            + "broker1.user=user1\n" + "broker1.password=p1\n" + "broker1.vhost=vhost1";
        addressMap = createForConfig(defaultParams, config);
        // URI for broker1
        assertMapping(addressMap, "host1", 123, "host2", 456, "user1", "p1", "vhost1");
        // URI not specified in the config
        assertMapping(addressMap, "host1", 124, "defaultHost", 1, "defaultUser", "defaultPassword",
                "defaultVhost");
        // URI not specified in the config
        assertMapping(addressMap, "unknown_host", 123, "defaultHost", 1, "defaultUser", "defaultPassword",
                "defaultVhost");

        // config with one broker, user name, password, vhost aren't set
        config = "broker_key.broker1=host1:123\n" + "broker1.host=host2\n" + "broker1.port=456";
        addressMap = createForConfig(defaultParams, config);
        // URI for broker1
        assertMapping(addressMap, "host1", 123, "host2", 456, AMQPConstants.DEFAULT_USER,
                AMQPConstants.DEFAULT_PASSWORD, AMQPConstants.DEFAULT_VHOST);
        // URI not specified in the config
        assertMapping(addressMap, "unknown_host", 789, "defaultHost", 1, "defaultUser", "defaultPassword",
                "defaultVhost");

        // config with two broker, some parameters aren't set
        config = "broker_key.broker1=host1:12\n" + "broker1.host=host2\n" + "broker1.port=34\n"
            + "broker1.user=user1\n" + "broker1.password=p1\n" + "broker_key.broker2=host3:56\n"
            + "broker2.host=host4\n" + "broker2.port=78\n";
        addressMap = createForConfig(defaultParams, config);
        // URI for broker1
        assertMapping(addressMap, "host1", 12, "host2", 34, "user1", "p1", AMQPConstants.DEFAULT_VHOST);
        // URI for broker2
        assertMapping(addressMap, "host3", 56, "host4", 78, AMQPConstants.DEFAULT_USER,
                AMQPConstants.DEFAULT_PASSWORD, AMQPConstants.DEFAULT_VHOST);
        // URI not specified in the config
        assertMapping(addressMap, "unknown_host", 123, "defaultHost", 1, "defaultUser", "defaultPassword",
                "defaultVhost");
    }

    void assertMapping(BrokerAddressMap addressMap, String host, int port, String expectedHost,
            int expectedPort, String expectedUser, String expectedPassword, String expectedVhost)
            throws Exception {
        URI uri = new URI("protocol://" + host + ":" + port);
        AMQPConnectionParameters params = addressMap.getBrokerForObject(uri);
        Assert.assertEquals(expectedHost, params.getHost());
        Assert.assertEquals(expectedPort, params.getPort());
        Assert.assertEquals(expectedUser, params.getUsername());
        Assert.assertEquals(expectedPassword, params.getPassword());
        Assert.assertEquals(expectedVhost, params.getVhost());
    }

}
