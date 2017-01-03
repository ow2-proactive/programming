package org.objectweb.proactive.extensions.processbuilder;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;


@Ignore("Ignored because it depends too much on the environment, but can be used for manual testing")
public class WindowsProcessBuilderTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "pwd";

    @Test
    public void runas() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(new OSUser(USERNAME, PASSWORD),
                null, null);

        Process process = processBuilder.command("hostname").start();
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
    }

    @Test(timeout = 7000)
    public void runas_destroy_process() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(new OSUser(USERNAME, PASSWORD),
                null, null);

        Process process = processBuilder.command("ping", "-n", "20", "localhost").start();
        process.destroy();
    }
}