package unitTests.vfsprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URISyntaxException;

import org.apache.commons.vfs.FileSystemException;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
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
    private int rmiDefaultPort;

    @Before
    public void readRMIDefaultPort() throws UnknownProtocolException {
        rmiDefaultPort = AbstractRemoteObjectFactory.getRemoteObjectFactory(
                org.objectweb.proactive.core.Constants.RMI_PROTOCOL_IDENTIFIER).getPort();
    }

    @Test
    public void testSimpleName() throws FileSystemException {
        fileName = parseURI("paprmi://hostname.com/service/address?proactive_vfs_provider_path=/dir/file.txt");

        assertEquals("paprmi", fileName.getScheme());
        assertNull(fileName.getUserName());
        assertNull(fileName.getPassword());
        assertEquals(rmiDefaultPort, fileName.getPort());
        assertEquals(rmiDefaultPort, fileName.getDefaultPort());
        assertEquals("hostname.com", fileName.getHostName());
        assertEquals("/service/address", fileName.getServicePath());
        assertEquals("/dir/file.txt", fileName.getPath());
        assertEquals("paprmi://hostname.com/service/address?proactive_vfs_provider_path=/", fileName
                .getRootURI());
        assertEquals("paprmi://hostname.com/service/address?proactive_vfs_provider_path=/dir/file.txt",
                fileName.getURI());
        assertEquals("rmi://hostname.com/service/address", fileName.getServerURL());
    }

    @Test
    public void testNameWithDefinedPortOneElementServiceWithoutRoot() throws FileSystemException {
        fileName = parseURI("paprmi://hostname.com:1234/serviceAddress?proactive_vfs_provider_path=");

        assertEquals("paprmi", fileName.getScheme());
        assertNull(fileName.getUserName());
        assertNull(fileName.getPassword());
        assertEquals(1234, fileName.getPort());
        assertEquals(rmiDefaultPort, fileName.getDefaultPort());
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
        fileName = parseURI("paprmi://hostname.com/serviceAddress?someServiceQuery=xxx");

        assertEquals("paprmi", fileName.getScheme());
        assertNull(fileName.getUserName());
        assertNull(fileName.getPassword());
        assertEquals(rmiDefaultPort, fileName.getPort());
        assertEquals(rmiDefaultPort, fileName.getDefaultPort());
        assertEquals("hostname.com", fileName.getHostName());
        assertEquals("/serviceAddress?someServiceQuery=xxx", fileName.getServicePath());
        assertEquals("/", fileName.getPath());
        assertEquals(
                "paprmi://hostname.com/serviceAddress?someServiceQuery=xxx?proactive_vfs_provider_path=/",
                fileName.getRootURI());
        assertEquals(
                "paprmi://hostname.com/serviceAddress?someServiceQuery=xxx?proactive_vfs_provider_path=/",
                fileName.getURI());
        assertEquals("rmi://hostname.com/serviceAddress?someServiceQuery=xxx", fileName.getServerURL());
    }

    @Test
    public void testNameUnnormalizedServicePathUnnormalizedFilePath() throws FileSystemException {
        fileName = parseURI("paprmi://hostname.com/service/../anotherService?proactive_vfs_provider_path=/dir/subdir/../anotherSubDir");

        assertEquals("paprmi", fileName.getScheme());
        assertNull(fileName.getUserName());
        assertNull(fileName.getPassword());
        assertEquals(rmiDefaultPort, fileName.getPort());
        assertEquals(rmiDefaultPort, fileName.getDefaultPort());
        assertEquals("hostname.com", fileName.getHostName());
        assertEquals("/service/../anotherService", fileName.getServicePath());
        assertEquals("/dir/anotherSubDir", fileName.getPath());
        assertEquals("paprmi://hostname.com/service/../anotherService?proactive_vfs_provider_path=/",
                fileName.getRootURI());
        assertEquals(
                "paprmi://hostname.com/service/../anotherService?proactive_vfs_provider_path=/dir/anotherSubDir",
                fileName.getURI());
        assertEquals("rmi://hostname.com/service/../anotherService", fileName.getServerURL());
    }

    @Test
    public void testServiceURLInteractionWithProActiveFileName() throws UnknownProtocolException,
            URISyntaxException, FileSystemException {
        final String serverURL = "rmi://hostname.com/service";
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
