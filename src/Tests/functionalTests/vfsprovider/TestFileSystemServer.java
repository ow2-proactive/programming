package functionalTests.vfsprovider;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.StreamMode;
import org.objectweb.proactive.extensions.vfsprovider.server.FileSystemServerImpl;

import unitTests.vfsprovider.AbstractIOOperationsBase;


/**
 * Simple usage test.
 */
public class TestFileSystemServer extends AbstractIOOperationsBase {

    @Override
    public String getTestDirFilename() {
        return "PROACTIVE-FileSystemServerFunctionalTest";
    }

    @Test
    public void test() throws IOException, StreamNotFoundException, WrongStreamTypeException {
        FileSystemServerImpl server = new FileSystemServerImpl(testDir.getAbsolutePath());
        server.startAutoClosing();
        final String path = "/" + TEST_FILENAME;
        final long stream = server.streamOpen(path, StreamMode.SEQUENTIAL_READ);
        final int len = (int) server.fileGetInfo(path).getSize() % Integer.MAX_VALUE;
        final byte[] content = server.streamRead(stream, len);
        Assert.assertArrayEquals(TEST_FILE_CONTENT.getBytes(), content);
        server.streamClose(stream);
        server.stopServer();
    }
}
