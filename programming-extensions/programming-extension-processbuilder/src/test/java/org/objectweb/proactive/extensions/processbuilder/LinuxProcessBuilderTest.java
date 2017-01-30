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
package org.objectweb.proactive.extensions.processbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.OperatingSystem;


public class LinuxProcessBuilderTest extends ProcessBuilderTest {

    public static final String PROCESSBUILDER_KEYPATH_PROPNAME = "runasme.key.path";

    private String proactiveHome;

    private String pathToSSHKey;

    @Before
    public void before() {
        assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.unix);

        assumeTrue(CentralPAPropertyRepository.PA_HOME.isSet());
        proactiveHome = CentralPAPropertyRepository.PA_HOME.getValue();

        pathToSSHKey = System.getProperty(PROCESSBUILDER_KEYPATH_PROPNAME);
        assumeTrue(pathToSSHKey != null);

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        ProActiveLogger.getLogger(Loggers.OSPB).setLevel(Level.DEBUG);
    }

    @Test
    public void sudo() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username), null, proactiveHome);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();
        System.out.println("[SUDO] process terminated with exit code " + exitCode);

        assertEquals(0, exitCode);
    }

    @Test
    public void sudoEnvVar() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username), null, proactiveHome);

        processBuilder.environment().put("MYVAR1", "MYVALUE1");
        processBuilder.environment().put("MYVAR2", "MYVALUE2");
        Process process = processBuilder.command("env").start();
        int exitCode = process.waitFor();
        System.out.println("[SUDO_ENVVAR] process terminated with exit code " + exitCode);
        assertEquals(0, exitCode);
        String output = IOUtils.toString(process.getInputStream());
        System.out.println(output);
        assertTrue(output.contains("MYVALUE1"));
        assertTrue(output.contains("MYVALUE2"));
    }

    @Test
    public void su() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username, password),
                                                                     null,
                                                                     proactiveHome);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();
        System.out.println("[SU] process terminated with exit code " + exitCode);

        assertEquals(0, exitCode);
    }

    @Test
    public void suEnvVar() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username, password),
                                                                     null,
                                                                     proactiveHome);

        processBuilder.environment().put("MYVAR1", "MYVALUE1");
        processBuilder.environment().put("MYVAR2", "MYVALUE2");
        Process process = processBuilder.command("env").start();
        int exitCode = process.waitFor();
        System.out.println("[SU_ENVAR] process terminated with exit code " + exitCode);
        assertEquals(0, exitCode);
        String output = IOUtils.toString(process.getInputStream());
        System.out.println(output);
        assertTrue(output.contains("MYVALUE1"));
        assertTrue(output.contains("MYVALUE2"));
    }

    @Test(timeout = 5000)
    public void su_destroy_process() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username, password),
                                                                     null,
                                                                     proactiveHome);
        Process process = processBuilder.command("sleep", "5").start();
        process.destroy();
        int exitCode = process.waitFor();
        System.out.println("[SU_DESTROYPROCESS] process terminated with exit code " + exitCode);
        assertEquals(0, exitCode);
    }

    @Test(timeout = 2000)
    public void su_destroy_process_gracefully() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username, password),
                                                                     null,
                                                                     proactiveHome);
        Process process = processBuilder.command("trap \"exit 5\" SIGTERM; echo $$; while true; do sleep 1; done")
                                        .start();
        process.destroy();
        int exitCode = process.waitFor();
        System.out.println("[SU_DESTROYPROCESS] process terminated with exit code " + exitCode);
        assertEquals(5, exitCode);
    }

    @Test(timeout = 15000)
    public void su_destroy_process_sigkill_after_graceful_timeout() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username, password),
                                                                     null,
                                                                     proactiveHome);
        System.setProperty("proactive.process.builder.cleanup.time.seconds", "1"); // Set SIGTERM timeout to 1 seconds
        Process process = processBuilder.command("trap \"echo trapped signal\" SIGTERM; echo $$; while true; do sleep 1; done")
                                        .start();
        process.destroy();
        int exitCode = process.waitFor();
        System.out.println("[SU_DESTROYPROCESS] process terminated with exit code " + exitCode);
        assertEquals(0, exitCode);
        System.clearProperty("proactive.process.builder.cleanup.time.seconds");
    }

    @Test
    public void ssh() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username,
                                                                                FileUtils.readFileToByteArray(new File(pathToSSHKey))),
                                                                     null,
                                                                     proactiveHome);

        Process process = processBuilder.command("whoami").start();
        int exitCode = process.waitFor();
        System.out.println("[SSH] process terminated with exit code " + exitCode);

        assertEquals(0, exitCode);
    }

    @Test
    public void sshEnvVar() throws Exception {
        LinuxProcessBuilder processBuilder = new LinuxProcessBuilder(new OSUser(username,
                                                                                FileUtils.readFileToByteArray(new File(pathToSSHKey))),
                                                                     null,
                                                                     proactiveHome);
        processBuilder.environment().put("MYVAR1", "MYVALUE1");
        processBuilder.environment().put("MYVAR2", "MYVALUE2");
        Process process = processBuilder.command("env").start();
        int exitCode = process.waitFor();
        System.out.println("[SSH_ENVVAR] process terminated with exit code " + exitCode);
        assertEquals(0, exitCode);
        String output = IOUtils.toString(process.getInputStream());
        System.out.println(output);
        assertTrue(output.contains("MYVALUE1"));
        assertTrue(output.contains("MYVALUE2"));
    }
}
