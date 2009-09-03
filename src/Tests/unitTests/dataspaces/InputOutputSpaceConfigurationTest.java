package unitTests.dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceType;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


public class InputOutputSpaceConfigurationTest {
    private static final String URL = "http://host/";
    private static final String PATH = "/file.txt";
    private static final String HOSTNAME = "host";
    private static final String NAME = "name";
    private static final SpaceType TYPE = SpaceType.INPUT;

    private InputOutputSpaceConfiguration config;

    @Test
    public void testCreateInputWithURLName() throws ConfigurationException {
        config = InputOutputSpaceConfiguration.createInputSpaceConfiguration(URL, null, null, NAME);
        assertProperlyConfigured(URL, null, null, NAME, SpaceType.INPUT, true);
    }

    @Test
    public void testCreateOutputWithURLName() throws ConfigurationException {
        config = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(URL, null, null, NAME);
        assertProperlyConfigured(URL, null, null, NAME, SpaceType.OUTPUT, true);
    }

    @Test
    public void testCreateWithURLPathHostnameNameType() throws ConfigurationException {
        config = InputOutputSpaceConfiguration.createConfiguration(URL, PATH, HOSTNAME, NAME, TYPE);
        assertProperlyConfigured(URL, PATH, HOSTNAME, NAME, TYPE, true);
    }

    @Test
    public void testCreateWithURLNameType() throws ConfigurationException {
        config = InputOutputSpaceConfiguration.createConfiguration(URL, null, null, NAME, TYPE);
        assertProperlyConfigured(URL, null, null, NAME, TYPE, true);
    }

    @Test
    public void testCreateWithURLHostnameNameType() throws ConfigurationException {
        // just check if it does not crash if there is no path
        config = InputOutputSpaceConfiguration.createConfiguration(URL, null, "hostname", NAME, TYPE);
    }

    @Test
    public void testCreateWithPathHostname() throws ConfigurationException {
        config = InputOutputSpaceConfiguration.createConfiguration(null, PATH, HOSTNAME, NAME, TYPE);
        assertProperlyConfigured(null, PATH, HOSTNAME, NAME, TYPE, false);
    }

    private void assertProperlyConfigured(String url, String path, String hostname, String name,
            SpaceType type, boolean complete) {
        assertEquals(url, config.getUrl());
        assertEquals(path, config.getPath());
        assertEquals(hostname, config.getHostname());
        assertEquals(name, config.getName());
        assertSame(type, config.getType());
        assertEquals(complete, config.isComplete());
    }

    @Test
    public void testTryCreateWithNameType() throws ConfigurationException {
        testTryCreateWrongConfig(null, null, null, NAME, TYPE);
    }

    @Test
    public void testTryCreateWithPathNameTypeNoHostname() throws ConfigurationException {
        testTryCreateWrongConfig(null, PATH, null, NAME, TYPE);
    }

    @Test
    public void testTryCreateWithURLNameWrongType() throws ConfigurationException {
        testTryCreateWrongConfig(URL, null, null, NAME, SpaceType.SCRATCH);
    }

    @Test
    public void testTryCreateWithURLNameNoType() throws ConfigurationException {
        testTryCreateWrongConfig(URL, null, null, NAME, null);
    }

    @Test
    public void testTryCreateWithURLTypeNoName() throws ConfigurationException {
        testTryCreateWrongConfig(URL, null, null, null, TYPE);
    }

    private void testTryCreateWrongConfig(String url, String path, String hostname, String name,
            SpaceType type) {
        try {
            config = InputOutputSpaceConfiguration.createConfiguration(url, path, hostname, name, type);
            fail("exception expected");
        } catch (ConfigurationException x) {
        }
    }

    @Test
    public void testEquals() throws ConfigurationException {
        config = InputOutputSpaceConfiguration.createConfiguration(URL, null, "hostname", NAME, TYPE);
        InputOutputSpaceConfiguration config2 = InputOutputSpaceConfiguration.createConfiguration(URL, null,
                "hostname", NAME, TYPE);
        InputOutputSpaceConfiguration config3 = InputOutputSpaceConfiguration.createConfiguration(URL, null,
                "hostname", NAME + "x", TYPE);

        assertEquals(config, config2);
        assertFalse(config.equals(config3));
        assertFalse(config3.equals(config));
    }
}
