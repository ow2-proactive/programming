/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionalTests.processbuilder.test;

import static org.junit.Assert.fail;
import static org.junit.Assume.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.objectweb.proactive.extensions.processbuilder.PAOSProcessBuilderFactory;
import org.objectweb.proactive.extensions.processbuilder.WindowsProcess;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;

import functionalTests.FunctionalTest;


/**
 * A set of various tests or the WindowsProcessBuilder, skipped on non-Windows os
 * @author Vladimir Bodnartchouk
 */
public class WindowsProcessBuilderTests extends FunctionalTest {

    final static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

    private static PAOSProcessBuilderFactory factory;

    private static OSUser osUser;

    @BeforeClass
    static public void setOSRuntime() throws ProActiveException {
        assumeTrue(isWindows); // run only on windows

        String user = System.getenv("OSPB_TEST_USER");
        assumeNotNull(user, "process builder not tested because OSPB_TEST_USER is not set");

        String pass = System.getenv("OSPB_TEST_PASS");
        assumeNotNull(pass, "process builder not tested because OSPB_TEST_PASS is not set");

        String domain = System.getenv("USERDOMAIN");
        osUser = new OSUser(user, pass);
        osUser.setDomain(domain);

        factory = new PAOSProcessBuilderFactory();
    }

    private void checkIsRunningOrFail(Process p) {
        try {
            int exitValue = p.exitValue();
            fail("When a process is running the exitValue() must throw an exception but it returns " + exitValue);
        } catch (IllegalThreadStateException e) {
            //ok the process is still running
        }
    }

    @org.junit.Test
    public void testProcessWaitForInterrupt() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        builder.command("cmd.exe");

        final ExecutorService exec = Executors.newSingleThreadExecutor();
        final Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
            // Let the worker thread start the process   
            final AtomicReference<Thread> waitingThreadRef = new AtomicReference<Thread>();
            Future<Boolean> f = exec.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    waitingThreadRef.set(Thread.currentThread());
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                        return true;
                    }
                    return false;
                }
            });

            // Wait for the waitingThread becomes available
            Thread waitingThread = waitingThreadRef.get();
            while (waitingThread == null) {
                Thread.sleep(1000);
                waitingThread = waitingThreadRef.get();
            }
            // Emit interruption
            waitingThread.interrupt();
            // Check that the process is still running
            try {
                int exitValue = p.exitValue();
                fail("After interruption the process still be running but it exited with value " + exitValue);
            } catch (IllegalThreadStateException e) {
                //ok the process is still running
            }

            boolean res = f.get(1000, TimeUnit.MILLISECONDS);
            org.junit.Assert.assertEquals("The waitFor() didn't throw the InterruptedException", true, res);
        } finally {
            if (p != null) {
                p.destroy();
            }
            exec.shutdown();
        }
    }

    /**
     * Start notepad.exe as subprocess in a separate thread then kills it the
     * waitFor method
     */
    @org.junit.Test
    public void testProcessWaitFor() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        builder.command("cmd.exe");

        final ExecutorService exec = Executors.newSingleThreadExecutor();
        final Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
            // Let the worker thread start the process
            final Future<Integer> f = exec.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return p.waitFor();
                }
            });

            // Then destroy the process and make sure the exit value is 1
            p.destroy();
            org.junit.Assert.assertEquals("Problem the exit code is incorrect (must be 1)", 1, (int) f.get());
        } finally {
            if (p != null) {
                p.destroy();
            }
            exec.shutdown();
        }
    }

    /**
     * This test creates a cmd.exe process (father) then the father creates a
     * child java process that waits 15 seconds. The father is destroyed (it
     * must kill also the child) then if the child pid appears in the list of
     * java processes listed by jps tool then the test fails.
     */
    @org.junit.Test
    public void testProcessKillChildren() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        builder.command("cmd.exe", "/c", "jrunscript");
        final Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
        // Now the first process (father) is terminated then we need to be sure that the child is alse
        // dead, for this we run the jps tool to be sure the child pid does not appear

        final Process pp = Runtime.getRuntime().exec(new String[] { "jps.exe", "-v" });

        // Use jps tool to list the java processes
        try {
            final InputStreamReader isr = new InputStreamReader(pp.getInputStream());
            final BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Main -Dapplication.home")) {
                    fail("PROBLEM: The child process was not killed, tree kill seems broken");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("PROBLEM: unable to use jps tool");
        }
    }

    @org.junit.Test
    public void testProcessStdout() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        final String s = "test";
        builder.command("cmd.exe", "/c", "echo " + s);
        Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!s.equals(line)) {
                    fail("PROBLEM: not same echo value = " + line);
                }
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    @org.junit.Test
    public void testProcessStderr() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        builder.command("cmd.exe", "/c", "echo test 2>&1");
        String s = "test";
        Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
            final InputStreamReader isr = new InputStreamReader(p.getErrorStream());
            final BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!s.equals(line)) {
                    fail("PROBLEM: not same echo value = " + line);
                }
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    @Test(expected = IOException.class)
    public void testProcessFileNotFound() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        builder.command("notepadd.exe");
        builder.start();
    }

    @Test(expected = OSUserException.class)
    public void testProcessLogonError() throws Exception {
        OSUser u = new OSUser("badlogin", "badpass");
        u.setDomain(System.getenv("USERDOMAIN"));
        OSProcessBuilder builder = factory.getBuilder(u);
        builder.command("notepad.exe");
        builder.start();
    }

    @Test
    public void testProcessEnv() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        builder.command("cmd.exe", "/c", "echo %USERNAME%");
        Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!osUser.getUserName().equals(line)) {
                    fail("PROBLEM: Unable to load the user env, the  WindowsProcess.internalGetUserEnv() seems broken");
                }
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    @Test
    public void testProcessOverrideEnv() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        Map<String, String> overrideEnv = builder.environment();
        final String valueToCheck = "tralalaasdf";
        overrideEnv.put("TEST_ENV_VAR", valueToCheck);
        builder.command("cmd.exe", "/c", "echo %TEST_ENV_VAR%");
        Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
            final InputStreamReader isr = new InputStreamReader(p.getInputStream());
            final BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!valueToCheck.equals(line)) {
                    fail("PROBLEM: Unable to override the process env, the WindowsProcess.internalGetUserEnv() seems broken");
                }
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    @Test
    public void testProcessDefaultWorkingDir() throws Exception {
        OSProcessBuilder builder = factory.getBuilder(osUser);
        builder.command("cmd.exe", "/c", "IF \"%CD%\"==\"%USERPROFILE%\" (echo ok) ELSE (echo %CD%)");
        Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
            final InputStreamReader isr = new InputStreamReader(p.getInputStream());
            final BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!"ok".equals(line)) {
                    fail("PROBLEM: The default working dir is not the user profile dir, %CD% is " + line);
                }
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    @Test
    public void testProcessOverrideWorkingDir() throws Exception {
        final String currentTemp = System.getenv("ALLUSERSPROFILE");
        if (currentTemp == null) {
            // cannot execute the test
            return;
        }
        OSProcessBuilder builder = factory.getBuilder(osUser);
        builder.directory(new File(currentTemp));
        builder.command("cmd.exe", "/c", "IF \"%CD%\"==\"" + currentTemp + "\" (echo ok) ELSE (echo %CD%)");
        Process p = builder.start();
        try {
            checkIsRunningOrFail(p);
            final InputStreamReader isr = new InputStreamReader(p.getInputStream());
            final BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!"ok".equals(line)) {
                    fail("PROBLEM: The default working dir is not the specified dir, %CD% is " + line);
                }
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }
}
