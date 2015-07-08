package org.objectweb.proactive.extensions.processbuilder;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;


@Ignore("Ignored because it depends too much on the environment, but can be used for manual testing")
public class LinuxProcessBuilderTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "pwd";
    private static final String PROACTIVE_HOME = "/Users/user/src/programming";
    private static final String PATH_TO_SSH_PRIVATE_KEY = "/Users/" + USERNAME + "/.ssh/id_rsa";

    @Test
    public void sudo() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME), null,
            PROACTIVE_HOME);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
    }

    @Test
    public void su() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME, PASSWORD), null,
            PROACTIVE_HOME);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
    }

    @Test(timeout = 5000)
    public void su_destroy_process() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME, PASSWORD), null,
          PROACTIVE_HOME);

        Process process = processBuilder.command("sleep", "5").start();
        process.destroy();
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
    }

    @Test
    public void ssh() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME, FileUtils
                .readFileToByteArray(new File(PATH_TO_SSH_PRIVATE_KEY))), null, PROACTIVE_HOME);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
    }
}