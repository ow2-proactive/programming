/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
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
