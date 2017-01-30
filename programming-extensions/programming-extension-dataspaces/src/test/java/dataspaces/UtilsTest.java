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

import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;


public class UtilsTest {
    @Test
    public void testGetLocalAccessURLMatchingHostname()
            throws org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException {
        final String hostname = Utils.getHostname();
        assertEquals("/local", Utils.getLocalAccessURL("http://remote/", "/local", hostname));
    }

    @Test
    public void testGetLocalAccessURLNonMatchingHostname() throws ConfigurationException {
        final String hostname = Utils.getHostname() + "haha";
        assertEquals("http://remote/", Utils.getLocalAccessURL("http://remote/", "/local", hostname));
    }

    @Test
    public void testGetLocalAccessURLNoLocalPath() throws ConfigurationException {
        assertEquals("http://remote/", Utils.getLocalAccessURL("http://remote/", null, null));
    }

    @Test
    public void testAppendSubDirsNoBaseLocation() throws Exception {
        assertNull(Utils.appendSubDirs(null, "abc"));
    }

    @Test
    public void testAppendSubDirsNoBaseNoSubDir() throws Exception {
        assertNull(Utils.appendSubDirs(null));
    }

    @Test
    public void testAppendSubDirsUnixBaseNoSlashNoSubDir() throws Exception {
        assertEquals("/abc", Utils.appendSubDirs("/abc"));
    }

    @Test
    public void testAppendSubDirsWindowsBaseNoSlashNoSubDir() throws Exception {
        assertEquals("c:\\abc", Utils.appendSubDirs("c:\\abc"));
    }

    @Test
    public void testAppendSubDirsUnixBaseSlash1SubDir() throws Exception {
        assertEquals("/abc/1", Utils.appendSubDirs("/abc/", "1"));
    }

    @Test
    public void testAppendSubDirsWindowsBaseSlash1SubDir() throws Exception {
        assertEquals("c:\\abc\\1", Utils.appendSubDirs("c:\\abc\\", "1"));
    }

    @Test
    public void testAppendSubDirsUnixBaseNoSlash1SubDir() throws Exception {
        assertEquals("/abc/1", Utils.appendSubDirs("/abc", "1"));
    }

    @Test
    public void testAppendSubDirsWindowsBaseNoSlash1SubDir() throws Exception {
        assertEquals("c:\\abc\\1", Utils.appendSubDirs("c:\\abc", "1"));
    }

    @Test
    public void testAppendSubDirsURLBase1SubDir() throws Exception {
        assertEquals("http://test.com/1", Utils.appendSubDirs("http://test.com/", "1"));
    }

    @Test
    public void testAppendSubDirsUnixBaseNoSlash2SubDir() throws Exception {
        assertEquals("/abc/1/2", Utils.appendSubDirs("/abc", "1", "2"));
    }
}
