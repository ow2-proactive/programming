package functionalTests.gcmdeployment.executable;

import java.io.File;
import java.io.IOException;


public class Executable {
    public static void main(String[] args) throws IOException {
        File tmpDir = new File(args[0]);
        System.out.println("tmpDir is " + tmpDir.toString() + " " + tmpDir.exists());
        File.createTempFile(Executable.class.getCanonicalName(), null, tmpDir);
    }
}
