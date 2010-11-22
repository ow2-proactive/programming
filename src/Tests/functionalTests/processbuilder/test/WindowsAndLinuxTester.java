package functionalTests.processbuilder.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.OSRuntime;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.objectweb.proactive.extensions.processbuilder.PAOSProcessBuilderFactory;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;
import org.objectweb.proactive.extensions.processbuilder.stream.LineReader;

import functionalTests.FunctionalTest;


public class WindowsAndLinuxTester extends FunctionalTest {
    final static boolean isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux");
    final static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

    // Modify these to fit your environment
    OSUser user;
    OSUser userWPass;
    String tempPath;

    static OSRuntime osRuntime;

    /*
     * It is assumed that the user given for testing purposes can both sudo and su into an other
     * user.
     */

    // ------------------------------------ 
    @Before
    public void shouldRun() throws ProActiveException {
        String suser = System.getenv("OSPB_TEST_USER");
        Assume.assumeNotNull(suser, "process builder not tested because OSPB_TEST_USER is not set");
        user = new OSUser(System.getenv("OSPB_TEST_USER"));

        String pass = System.getenv("OSPB_TEST_PASS");
        Assume.assumeNotNull(pass, "process builder not tested because OSPB_TEST_PASS is not set");
        userWPass = new OSUser(suser, pass);

        tempPath = System.getenv("OSPB_TEST_TEMP");
        Assume.assumeNotNull(tempPath, "process builder not tested because OSPB_TEST_TEMP is not set");

        if (isLinux) {
            String paHome = ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome();
            File s32 = new File(paHome, "dist/scripts/processbuilder/linux/suer32");
            File s64 = new File(paHome, "dist/scripts/processbuilder/linux/suer64");
            Assume.assumeTrue(s32.exists() || s64.exists());
        }
    }

    @BeforeClass
    static public void setOSRuntime() throws ProActiveException {
        osRuntime = new OSRuntime();
    }

    @Test
    public void fallTroughIfNoUser() {
        String[] lc = { "/bin/sh", "-c", "echo 111 && (echo 222 >&2)" };
        String[] wc = { "cmd.exe", "/c", "@(echo 111)&&(echo 222>&2)" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = { "111" };
        String[] expectedErr = { "222" };

        try {
            runAndMatch(cmd, null, dir, env, expectedOut, expectedErr, 0);
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }
    }

    @Test
    public void OutAndErrorChannelsExist() {
        String[] lc = { "/bin/sh", "-c", "echo 111 && (echo 222 >&2)" };
        String[] wc = { "cmd.exe", "/c", "@(echo 111)&& (echo 222>&2)" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = { "111" };
        String[] expectedErr = { "222" };

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, 0);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, 0);
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

    }

    @Test
    public void checkWhoami() {
        String[] lc = { "/bin/sh", "-c", "echo $USER" };
        String[] wc = { "cmd.exe", "/c", "@echo %USERDOMAIN%\\%USERNAME%" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = { user.getUserName() };
        if (isWindows) {
            expectedOut[0] = System.getenv().get("USERDOMAIN") + "\\" + user.getUserName();
        }

        String[] expectedErr = {};

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, 0);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, 0);
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }
    }

    @Test
    public void checkCanExecuteAsUser() throws Exception {
        OSProcessBuilder ospbUser = new PAOSProcessBuilderFactory().getBuilder(user);
        OSProcessBuilder ospbUserWPass = new PAOSProcessBuilderFactory().getBuilder(userWPass);
        if (!isWindows) {
            assertTrue(ospbUser.canExecuteAsUser(user));
        }
        assertTrue(ospbUserWPass.canExecuteAsUser(userWPass));
        if (!isWindows) {
            assertFalse(ospbUser.canExecuteAsUser(new OSUser(user.getUserName(), "jibberish")));
            assertFalse(ospbUser.canExecuteAsUser(new OSUser("jibberish")));
        }
    }

    @Test
    public void invalidUserName() {
        String[] lc = { "/bin/sh", "-c", "echo $USER" };
        String[] wc = { "cmd.exe", "/c", "@echo %USERDOMAIN%\\%USERNAME%" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = {};
        String[] expectedErr = {};

        try {
            runAndMatch(cmd, new OSUser("xyz"), dir, env, expectedOut, expectedErr, null);
        } catch (OSUserException osue) {
            // we are good, we expected this
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

    }

    @Test
    public void invalidUserNameAndPass() {
        String[] lc = { "/bin/sh", "-c", "echo $USER" };
        String[] wc = { "cmd.exe", "/c", "@echo %USERDOMAIN%\\%USERNAME%" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = {};
        String[] expectedErr = {};

        try {
            runAndMatch(cmd, new OSUser("xyz", "xyz"), dir, env, expectedOut, expectedErr, null);
        } catch (OSUserException osue) {
            // we are good, we expected this
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

    }

    @Test
    public void invalidCommand() throws IOException, InterruptedException {
        String[] lc = { "/bin/sh/" };
        String[] wc = { "cmd__.exe" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = {};
        String[] expectedErr = {};

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, null);
            } catch (IOException e) {
                // expected
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, null);
        } catch (IOException e) {
            // expected
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

        String[] lc2 = { "/bin/sh", "-c", "toto'" };
        String[] wc2 = { "cmd.exe", "/c", "toto'" };
        cmd = (isLinux) ? lc2 : wc2;

        //create reference value
        Process p = Runtime.getRuntime().exec(cmd);
        expectedErr = getError(p);
        //        if (!isLinux) {
        //            //windows will output a shorter error message if the command is 
        //            //started from inside an other command... at east it seems so
        //            String err[] = new String[1];
        //            err[0] = expectedErr[0];
        //            expectedErr = err;
        //        }
        expectedOut = getOutput(p);
        p.waitFor();

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, null);
            } catch (IOException e) {
                // expected
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, null);
        } catch (IOException e) {
            // expected
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

    }

    @Test
    public void invalidCommandWithNoUser() {
        String[] lc = { "/bin/sh/" };
        String[] wc = { "cmd__.exe" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = {};
        String[] expectedErr = {};

        try {
            runAndMatch(cmd, null, dir, env, expectedOut, expectedErr, null);
        } catch (IOException e) {
            // expected
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

    }

    @Test
    public void exitValues() {
        String[] lc = { "/bin/sh", "-c", " exit 1" };
        String[] wc = { "cmd.exe", "/c", "@exit 1" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = {};
        String[] expectedErr = {};
        Integer exitValue = 1;

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, exitValue);
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

        String[] lc2 = { "/bin/sh", "-c", " exit 111" };
        String[] wc2 = { "cmd.exe", "/c", "@exit 111" };
        cmd = (isLinux) ? lc2 : wc2;
        exitValue = 111;

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, exitValue);
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

        String[] lc3 = { "/bin/sh", "-c", " exit 250" };
        String[] wc3 = { "cmd.exe", "/c", "@exit -1" };
        cmd = (isLinux) ? lc3 : wc3;
        exitValue = (isLinux) ? 250 : -1;

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, exitValue);
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }
    }

    @Test
    public void lightUnicodeCompatibility() {
        String unicode = "Antonín Dvořák";
        String[] lc = { "/bin/sh", "-c", " echo " + unicode };
        String[] wc = { "cmd.exe", "/c", "@echo " + unicode };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = null;

        String[] expectedOut = { unicode };
        String[] expectedErr = {};
        Integer exitValue = 0;

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        // the default behavior (with Runtime.exec) is not accepted by this test
        if (!isWindows) {
            try {
                runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

    }

    @Test
    public void checkWorkingDir() throws IOException {
        String[] lc = { "/bin/sh", "-c", "echo `pwd`" };
        String[] wc = { "cmd.exe", "/c", "@echo %CD%" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = new File("../");
        HashMap<String, String> env = null;

        String[] expectedOut = { dir.getCanonicalPath() };
        String[] expectedErr = {};
        Integer exitValue = 0;

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, exitValue);
        } catch (Exception e) {
            assertNull(e.getMessage(), e);
        }

        dir = new File(tempPath + "sp a c e");
        assertTrue(dir.mkdir());
        expectedOut[0] = dir.getCanonicalPath();

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                dir.delete();
                assertNull(e.getMessage(), e);
            }
        }

        try {
            runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, exitValue);
        } catch (Exception e) {
            dir.delete();
            assertNull(e.getMessage(), e);
        }

        dir.delete();
    }

    @Test
    public void checkEnvironmentPropagation() {
        String[] lc = { "/bin/sh", "-c", " echo $TEST_ENV_VAR" };
        String[] wc = { "cmd.exe", "/c", "echo %TEST_ENV_VAR%" };
        String[] cmd = (isLinux) ? lc : wc;
        File dir = null;
        HashMap<String, String> env = new HashMap<String, String>();
        String outp = "it seems to work";
        env.put("TEST_ENV_VAR", outp);

        String[] expectedOut = { outp };
        String[] expectedErr = {};
        Integer exitValue = 0;

        if (!isWindows) {
            try {
                runAndMatch(cmd, user, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

        // On Windows the behaviour is not defined yet
        // if the forked process as user inherits the environment from its creator it
        // can represent a security issue ..
        if (!isWindows) {
            try {
                runAndMatch(cmd, userWPass, dir, env, expectedOut, expectedErr, exitValue);
            } catch (Exception e) {
                assertNull(e.getMessage(), e);
            }
        }

    }

    public static void runAndMatch(String[] cmd, OSUser user, File dir, Map<String, String> env,
            String[] expectedOut, String[] expectedError, Integer expectedRet) throws Exception {
        Process p = (user != null) ? osRuntime.exec(user, cmd, env, dir) : osRuntime.exec(cmd, env, dir);
        String[] out = getOutput(p);
        String[] err = getError(p);
        String errors = "";

        if (out.length == expectedOut.length && err.length == expectedError.length) {
            //check output
            for (int i = 0; i < out.length; i++) {
                if (!out[i].equals(expectedOut[i]))
                    errors += ("Outputs don't match at line " + i + "\n I have \"" + out[i] +
                        "\" instead of \"" + expectedOut[i] + "\"\n");
            }
            //check error
            for (int i = 0; i < err.length; i++) {
                if (!err[i].equals(expectedError[i]))
                    errors += ("Errors don't match at line " + i + "\n I have \"" + err[i] +
                        "\" instead of \"" + expectedError[i] + "\"\n");
            }
        } else {
            errors += ("Output lengths does not match!\nReal Out:" + out.length + " Exp. Out:" +
                expectedOut.length + "\nReal Err: " + err.length + " Exp. Err:" + expectedError.length);
        }
        p.waitFor();
        int eval = p.exitValue();
        if (expectedRet != null && !expectedRet.equals(eval))
            errors += ("Exit value is " + eval + " instead of " + expectedRet);

        if (errors.length() > 0) {
            throw new Exception(errors);
        }
    }

    public static String[] getOutput(Process p) {
        ArrayList<String> list = new ArrayList<String>();
        InputStream inputstream = p.getInputStream();
        LineReader bufferedreader = new LineReader(inputstream);
        String line;
        while ((line = bufferedreader.readLine()) != null) {
            list.add(line);
        }
        return list.toArray(new String[0]);
    }

    public static String[] getError(Process p) {
        ArrayList<String> list = new ArrayList<String>();
        InputStream inputstream = p.getErrorStream();
        LineReader bufferedreader = new LineReader(inputstream);
        String line;

        while ((line = bufferedreader.readLine()) != null) {
            list.add(line);
        }
        return list.toArray(new String[0]);
    }
}
