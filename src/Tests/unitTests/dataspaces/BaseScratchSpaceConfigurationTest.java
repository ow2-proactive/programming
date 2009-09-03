package unitTests.dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.ScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceType;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


public class BaseScratchSpaceConfigurationTest {

    private BaseScratchSpaceConfiguration config;

    @Test
    public void testCreateBaseConfigurationWithStraightUrlNoPath() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration("http://test.com/", null);
        assertEquals("http://test.com/", config.getUrl());
        assertNull(config.getPath());
    }

    @Test
    public void testCreateBaseConfigurationWithTemplatedUrlNoPath() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration("http://#{hostname}/", null);
        assertEquals("http://" + Utils.getHostname() + "/", config.getUrl());
        assertNull(config.getPath());
    }

    @Test
    public void testCreateBaseConfigurationWithPathNoUrl() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration(null, "/abc");
        assertNull(config.getUrl());
        assertEquals("/abc", config.getPath());
    }

    @Test
    public void testDeriveScratchSpaceConfiguration() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration("http://#{hostname}/", "/abc");
        final ScratchSpaceConfiguration derivedConfig = config.createScratchSpaceConfiguration("subdir");
        assertEquals("http://" + Utils.getHostname() + "/subdir", derivedConfig.getUrl());
        assertEquals("/abc/subdir", derivedConfig.getPath());
        assertEquals(Utils.getHostname(), derivedConfig.getHostname());
        assertEquals(SpaceType.SCRATCH, derivedConfig.getType());
    }

    @Test
    public void testTryCreateBaseConfigurationNoUrlNoPath() throws ConfigurationException {
        try {
            config = new BaseScratchSpaceConfiguration(null, null);
            fail("exception expected");
        } catch (ConfigurationException x) {
        }
    }
}
