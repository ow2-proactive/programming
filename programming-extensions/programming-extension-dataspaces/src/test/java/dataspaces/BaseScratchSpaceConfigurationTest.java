/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Assert;
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
        assertEquals("http://test.com/", config.getUrls()[0]);
        assertNull(config.getPath());
    }

    @Test
    public void testCreateBaseConfigurationWithTemplatedUrlNoPath() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration("http://" + BaseScratchSpaceConfiguration.HOSTNAME_VARIABLE_KEYWORD +
                                                   "/", null);
        assertEquals("http://" + Utils.getHostname() + "/", config.getUrls()[0]);
        assertNull(config.getPath());
    }

    @Test
    public void testCreateBaseConfigurationWithPathNoUrl() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration((String) null, "/abc");
        assertNull(config.getUrls());
        assertEquals("/abc", config.getPath());
    }

    @Test
    public void testCreateBaseConfigurationWithMultiUrlsIncludingFile() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration(new String[] { "file:///abc", "http://test.com/",
                                                                  "http://" + BaseScratchSpaceConfiguration.HOSTNAME_VARIABLE_KEYWORD +
                                                                                                     "/" },
                                                   "/abc");
        Assert.assertArrayEquals(new String[] { "file:///abc/", "http://test.com/",
                                                "http://" + Utils.getHostname() + "/" },
                                 config.getUrls());
        assertEquals("/abc", config.getPath());
    }

    @Test
    public void testCreateBaseConfigurationWithMultiUrlsNotIncludingFile() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration(new String[] { "http://test.com/",
                                                                  "http://" + BaseScratchSpaceConfiguration.HOSTNAME_VARIABLE_KEYWORD +
                                                                                      "/" },
                                                   "/abc");
        Assert.assertArrayEquals(new String[] { new File("/abc").toURI().toString(), "http://test.com/",
                                                "http://" + Utils.getHostname() + "/" },
                                 config.getUrls());
        assertEquals("/abc", config.getPath());
    }

    @Test
    public void testDeriveScratchSpaceConfiguration() throws ConfigurationException {
        config = new BaseScratchSpaceConfiguration("http://" + BaseScratchSpaceConfiguration.HOSTNAME_VARIABLE_KEYWORD +
                                                   "/", "/abc");
        final ScratchSpaceConfiguration derivedConfig = config.createScratchSpaceConfiguration("subdir");
        assertEquals("http://" + Utils.getHostname() + "/subdir/",
                     derivedConfig.getUrls().get(derivedConfig.getUrls().size() - 1));
        assertEquals("/abc/subdir", derivedConfig.getPath());
        assertEquals(Utils.getHostname(), derivedConfig.getHostname());
        assertEquals(SpaceType.SCRATCH, derivedConfig.getType());
    }

    @Test
    public void testTryCreateBaseConfigurationNoUrlNoPath() throws ConfigurationException {
        try {
            config = new BaseScratchSpaceConfiguration((String) null, null);
            fail("exception expected");
        } catch (ConfigurationException x) {
        }
    }
}
