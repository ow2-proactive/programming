package org.objectweb.proactive.extensions.processbuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@Ignore("Ignored because it depends too much on the environment, but can be used for manual testing")
public class LinuxProcessBuilderTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "pwd";
    private static final String PROACTIVE_HOME = "/tmp/programming";
    private static final String PATH_TO_SSH_PRIVATE_KEY = "/tmp/.ssh/id_rsa";

    @Before
    public void before() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        ProActiveLogger.getLogger(Loggers.OSPB).setLevel(Level.DEBUG);
    }

    @Test
    public void sudo() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME), null,
            PROACTIVE_HOME);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
    }

    @Test
    public void sudoEnvVar() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME), null,
            PROACTIVE_HOME);

        processBuilder.environment().put("MYVAR1", "MYVALUE1");
        processBuilder.environment().put("MYVAR2", "MYVALUE2");
        Process process = processBuilder.command("env").start();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
        String output = IOUtils.toString(process.getInputStream());
        System.out.println(output);
        assertTrue(output.contains("MYVALUE1"));
        assertTrue(output.contains("MYVALUE2"));
    }

    @Test
    public void su() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME, PASSWORD), null,
            PROACTIVE_HOME);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
    }

    @Test
    public void suEnvVar() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME, PASSWORD), null,
            PROACTIVE_HOME);

        processBuilder.environment().put("MYVAR1", "MYVALUE1");
        processBuilder.environment().put("MYVAR2", "MYVALUE2");
        Process process = processBuilder.command("env").start();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
        String output = IOUtils.toString(process.getInputStream());
        System.out.println(output);
        assertTrue(output.contains("MYVALUE1"));
        assertTrue(output.contains("MYVALUE2"));
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
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME,
            FileUtils.readFileToByteArray(new File(PATH_TO_SSH_PRIVATE_KEY))), null, PROACTIVE_HOME);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
    }

    @Test
    public void sshEnvVar() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(USERNAME,
            FileUtils.readFileToByteArray(new File(PATH_TO_SSH_PRIVATE_KEY))), null, PROACTIVE_HOME);
        processBuilder.environment().put("MYVAR1", "MYVALUE1");
        processBuilder.environment().put("MYVAR2", "MYVALUE2");
        Process process = processBuilder.command("env").start();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
        String output = IOUtils.toString(process.getInputStream());
        System.out.println(output);
        assertTrue(output.contains("MYVALUE1"));
        assertTrue(output.contains("MYVALUE2"));
    }
}