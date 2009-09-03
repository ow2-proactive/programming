package unitTests.vfsprovider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;


@Ignore
public abstract class AbstractIOOperationsBase {

    protected static final String TEST_FILENAME = "test.txt";
    protected static final String TEST_FILE_CONTENT = "qwerty";
    protected static final int TEST_FILE_CONTENT_LEN = TEST_FILE_CONTENT.getBytes().length;
    protected File testFile;
    protected File testDir;

    public static boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null)
                for (File ch : children)
                    deleteRecursively(ch);
        }
        return file.delete();
    }

    public String getTestDirFilename() {
        return "ProActive-AbstractIOOperationsBase";
    }

    @Before
    public void createFiles() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), getTestDirFilename());
        testFile = new File(testDir, TEST_FILENAME);
        assertTrue(testDir.mkdirs());
        assertTrue(testFile.createNewFile());

        final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(testFile));
        osw.write(TEST_FILE_CONTENT);
        osw.close();
    }

    @After
    public void deleteFiles() throws IOException {
        if (testDir != null)
            deleteRecursively(testDir);
        assertFalse(testDir.exists());
        testDir = null;
    }
}
