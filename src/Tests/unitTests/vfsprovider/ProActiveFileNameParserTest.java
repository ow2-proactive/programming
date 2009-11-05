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
package unitTests.vfsprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URISyntaxException;

import org.apache.commons.vfs.FileSystemException;
import org.junit.Test;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileName;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileNameParser;


/**
 * ProActiveFileNameParser (and somehow ProActiveFileName) basic tests.
 */
public class ProActiveFileNameParserTest {
    private static ProActiveFileName parseURI(final String uri) throws FileSystemException {
        return (ProActiveFileName) ProActiveFileNameParser.getInstance().parseUri(null, null, uri);
    }

    private ProActiveFileName fileName;

    @Test
    public void testSimpleName() throws FileSystemException {
        fileName = parseURI("paprmi://hostname.com:3232/service/address?proactive_vfs_provider_path=/dir/file.txt");

        assertEquals("paprmi", fileName.getScheme());
        assertNull(fileName.getUserName());
        assertNull(fileName.getPassword());
        assertEquals(3232, fileName.getPort());
        //        assertEquals(3232, fileName.getDefaultPort());
        assertEquals("hostname.com", fileName.getHostName());
        assertEquals("/service/address", fileName.getServicePath());
        assertEquals("/dir/file.txt", fileName.getPath());
        assertEquals("paprmi://hostname.com:3232/service/address?proactive_vfs_provider_path=/", fileName
                .getRootURI());
        assertEquals("paprmi://hostname.com:3232/service/address?proactive_vfs_provider_path=/dir/file.txt",
                fileName.getURI());
        assertEquals("rmi://hostname.com:3232/service/address", fileName.getServerURL());
    }

    @Test
    public void testNameWithDefinedPortOneElementServiceWithoutRoot() throws FileSystemException {
        fileName = parseURI("paprmi://hostname.com:1234/serviceAddress?proactive_vfs_provider_path=");

        assertEquals("paprmi", fileName.getScheme());
        assertNull(fileName.getUserName());
        assertNull(fileName.getPassword());
        assertEquals(1234, fileName.getPort());
        //        assertEquals(rmiDefaultPort, fileName.getDefaultPort());
        assertEquals("hostname.com", fileName.getHostName());
        assertEquals("/serviceAddress", fileName.getServicePath());
        assertEquals("/", fileName.getPath());
        assertEquals("paprmi://hostname.com:1234/serviceAddress?proactive_vfs_provider_path=/", fileName
                .getRootURI());
        assertEquals("paprmi://hostname.com:1234/serviceAddress?proactive_vfs_provider_path=/", fileName
                .getURI());
        assertEquals("rmi://hostname.com:1234/serviceAddress", fileName.getServerURL());
    }

    @Test
    public void testNameWithServiceIncludingQueryWithoutServiceAndFilePathSeparators()
            throws FileSystemException {
        fileName = parseURI("paprmi://hostname.com:3232/serviceAddress?someServiceQuery=xxx");

        assertEquals("paprmi", fileName.getScheme());
        assertNull(fileName.getUserName());
        assertNull(fileName.getPassword());
        assertEquals(3232, fileName.getPort());
        //        assertEquals(rmiDefaultPort, fileName.getDefaultPort());
        assertEquals("hostname.com", fileName.getHostName());
        assertEquals("/serviceAddress?someServiceQuery=xxx", fileName.getServicePath());
        assertEquals("/", fileName.getPath());
        assertEquals(
                "paprmi://hostname.com:3232/serviceAddress?someServiceQuery=xxx?proactive_vfs_provider_path=/",
                fileName.getRootURI());
        assertEquals(
                "paprmi://hostname.com:3232/serviceAddress?someServiceQuery=xxx?proactive_vfs_provider_path=/",
                fileName.getURI());
        assertEquals("rmi://hostname.com:3232/serviceAddress?someServiceQuery=xxx", fileName.getServerURL());
    }

    @Test
    public void testNameUnnormalizedServicePathUnnormalizedFilePath() throws FileSystemException {
        fileName = parseURI("paprmi://hostname.com:3232/service/../anotherService?proactive_vfs_provider_path=/dir/subdir/../anotherSubDir");

        assertEquals("paprmi", fileName.getScheme());
        assertNull(fileName.getUserName());
        assertNull(fileName.getPassword());
        assertEquals(3232, fileName.getPort());
        //        assertEquals(rmiDefaultPort, fileName.getDefaultPort());
        assertEquals("hostname.com", fileName.getHostName());
        assertEquals("/service/../anotherService", fileName.getServicePath());
        assertEquals("/dir/anotherSubDir", fileName.getPath());
        assertEquals("paprmi://hostname.com:3232/service/../anotherService?proactive_vfs_provider_path=/",
                fileName.getRootURI());
        assertEquals(
                "paprmi://hostname.com:3232/service/../anotherService?proactive_vfs_provider_path=/dir/anotherSubDir",
                fileName.getURI());
        assertEquals("rmi://hostname.com:3232/service/../anotherService", fileName.getServerURL());
    }

    @Test
    public void testServiceURLInteractionWithProActiveFileName() throws UnknownProtocolException,
            URISyntaxException, FileSystemException {
        final String serverURL = "rmi://hostname.com:3232/service";
        final String vfsURL = ProActiveFileName.getServerVFSRootURL(serverURL);
        assertEquals(serverURL, parseURI(vfsURL).getServerURL());
    }

    @Test(expected = FileSystemException.class)
    public void testBadNameWithUnknownScheme() throws Exception {
        parseURI("totototo://hostname.com/service?proactive_vfs_provider_path=/file.txt");
    }

    @Test(expected = FileSystemException.class)
    public void testBadNameWithoutServiceBeginningSlash() throws Exception {
        parseURI("totototo://hostname.com");
    }
}
