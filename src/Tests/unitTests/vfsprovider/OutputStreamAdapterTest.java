package unitTests.vfsprovider;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.server.OutputStreamAdapter;
import org.objectweb.proactive.extensions.vfsprovider.server.Stream;


public class OutputStreamAdapterTest extends AbstractStreamBase {

    @Override
    protected Stream getInstance(File f) throws Exception {
        return new OutputStreamAdapter(f, false);
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void skipTest() throws IOException, WrongStreamTypeException {
        super.skipTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void skipTestMore() throws IOException, WrongStreamTypeException {
        super.skipTestMore();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void skipTestZero() throws IOException, WrongStreamTypeException {
        super.skipTestZero();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void readMoreTest() throws IOException, WrongStreamTypeException {
        super.readMoreTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void readTest() throws IOException, WrongStreamTypeException {
        super.readTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void readZeroTest() throws IOException, WrongStreamTypeException {
        super.readZeroTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void getLengthTest() throws IOException, WrongStreamTypeException {
        super.getLengthTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void getLengthAfterChange() throws Exception {
        super.getLengthAfterChange();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void getPositionTest() throws Exception {
        super.getPositionTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void seekTest() throws IOException, WrongStreamTypeException {
        super.seekTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void seekAndGetLengthTest() throws IOException, WrongStreamTypeException {
        super.seekAndGetLengthTest();
    }
}
