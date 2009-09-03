package unitTests.vfsprovider;

import java.io.File;

import org.objectweb.proactive.extensions.vfsprovider.server.RandomAccessStreamAdapter;
import org.objectweb.proactive.extensions.vfsprovider.server.Stream;


public class RandomReadWriteAdapterTest extends AbstractStreamBase {

    @Override
    protected Stream getInstance(File f) throws Exception {
        return RandomAccessStreamAdapter.createRandomAccessReadWrite(f);
    }

    @Override
    protected long changePosition(Stream s) throws Exception {
        s.seek(10);
        return 10;
    }

    @Override
    protected long changeFileLength(Stream s) throws Exception {
        s.seek(1);
        s.write(TEST_FILE_CONTENT.getBytes());
        s.write(TEST_FILE_CONTENT.getBytes());
        return 2 * TEST_FILE_CONTENT_LEN + 1;
    }

}
