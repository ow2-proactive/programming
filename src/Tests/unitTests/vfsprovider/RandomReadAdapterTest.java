package unitTests.vfsprovider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.server.RandomAccessStreamAdapter;
import org.objectweb.proactive.extensions.vfsprovider.server.Stream;


public class RandomReadAdapterTest extends AbstractStreamBase {

    @Override
    protected Stream getInstance(File f) throws Exception {
        return RandomAccessStreamAdapter.createRandomAccessRead(f);
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void writeTest() throws IOException, WrongStreamTypeException {
        super.writeTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void flushTest() throws IOException, WrongStreamTypeException {
        super.flushTest();
    }

    @Override
    @Test(expected = FileNotFoundException.class)
    public void createFromNotExistingFileTest() throws Exception {
        super.createFromNotExistingFileTest();
    }

    @Override
    protected long changePosition(Stream s) throws Exception {
        s.seek(10);
        return 10;
    }
}
