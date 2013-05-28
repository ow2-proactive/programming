/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
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
        assertEquals("http://" + Utils.getHostname() + "/subdir", derivedConfig.getUrls().get(
                derivedConfig.getUrls().size() - 1));
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
