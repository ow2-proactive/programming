package unitTests.dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.core.ScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceType;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


public class ScratchSpaceConfigurationTest {
    private static final String URL = "http://host/";
    private static final String PATH = "/file.txt";
    private static final String HOSTNAME = "host";

    private ScratchSpaceConfiguration config;

    @Test
    public void testCreateWithURLPathHostname() throws ConfigurationException {
        config = new ScratchSpaceConfiguration(URL, PATH, HOSTNAME);
        assertProperlyConfigured(URL, PATH, HOSTNAME, true);
    }

    @Test
    public void testCreateWithURL() throws ConfigurationException {
        config = new ScratchSpaceConfiguration(URL, null, null);
        assertProperlyConfigured(URL, null, null, true);
    }

    @Test
    public void testCreateWithPathHostname() throws ConfigurationException {
        config = new ScratchSpaceConfiguration(null, PATH, HOSTNAME);
        assertProperlyConfigured(null, PATH, HOSTNAME, false);
    }

    private void assertProperlyConfigured(String url, String path, String hostname, boolean complete) {
        assertEquals(url, config.getUrl());
        assertEquals(path, config.getPath());
        assertEquals(hostname, config.getHostname());
        assertEquals(SpaceType.SCRATCH, config.getType());
        assertEquals(complete, config.isComplete());
    }

    @Test
    public void testTryCreateWithNothing() throws ConfigurationException {
        try {
            config = new ScratchSpaceConfiguration(null, null, null);
            fail("exception expected");
        } catch (ConfigurationException x) {
        }
    }

    @Test
    public void testTryCreateWithPathNoHostname() throws ConfigurationException {
        try {
            config = new ScratchSpaceConfiguration(null, PATH, null);
            fail("exception expected");
        } catch (ConfigurationException x) {
        }
    }

    @Test
    public void testEquals() throws ConfigurationException {
        config = new ScratchSpaceConfiguration(null, PATH, HOSTNAME);
        ScratchSpaceConfiguration config2 = new ScratchSpaceConfiguration(null, PATH, HOSTNAME);
        ScratchSpaceConfiguration config3 = new ScratchSpaceConfiguration(null, PATH, HOSTNAME + "x");

        assertEquals(config, config2);
        assertFalse(config.equals(config3));
        assertFalse(config3.equals(config));
    }
}
