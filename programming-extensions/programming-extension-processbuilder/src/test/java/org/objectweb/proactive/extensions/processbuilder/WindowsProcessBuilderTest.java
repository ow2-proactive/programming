package org.objectweb.proactive.extensions.processbuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.OperatingSystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;


public class WindowsProcessBuilderTest extends ProcessBuilderTest {

    @Before
    public void before() {
        assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.windows);

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        ProActiveLogger.getLogger(Loggers.OSPB).setLevel(Level.DEBUG);
    }


    @Test
    public void runas() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(new OSUser(username, password),
                null, null);

        Process process = processBuilder.command("hostname").start();
        int exitCode = process.waitFor();

        System.out.println("[RUNAS] Process finished with exit code : " + exitCode);

        assertEquals(0, exitCode);
    }

    @Test(timeout = 7000)
    public void runas_destroy_process() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(new OSUser(username, password),
                null, null);

        Process process = processBuilder.command("ping", "-n", "20", "localhost").start();
        process.destroy();

        System.out.println("[RUNAS_DESTROYPROCESS] Process finished with exit code : " + process.waitFor());
    }

    @Test
    public void runasEnvVar() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(new OSUser(username, password),
                null, null);

        processBuilder.environment().put("MYVAR1", "MYVALUE1");
        processBuilder.environment().put("MYVAR2", "MYVALUE2");

        Process process = processBuilder.command("cmd.exe", "/c", "set").start();
        int exitCode = process.waitFor();
        System.out.println("[RUNAS_ENVAR] Process finished with exit code : " + exitCode);

        assertEquals(0, exitCode);

        String output = IOUtils.toString(process.getInputStream());
        System.out.println(output);
        assertTrue(output.contains("MYVALUE1"));
        assertTrue(output.contains("MYVALUE2"));
    }
}