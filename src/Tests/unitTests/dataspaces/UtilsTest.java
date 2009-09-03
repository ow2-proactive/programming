package unitTests.dataspaces;

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
