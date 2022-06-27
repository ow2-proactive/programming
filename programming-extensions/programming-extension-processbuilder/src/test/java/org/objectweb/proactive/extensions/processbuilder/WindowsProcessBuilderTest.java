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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;
import org.objectweb.proactive.utils.OperatingSystem;


public class WindowsProcessBuilderTest extends ProcessBuilderTest {

    OSUser osUser;

    @Before
    public void before() throws ProActiveException {
        assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.windows);

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        ProActiveLogger.getLogger(Loggers.OSPB).setLevel(Level.DEBUG);

        osUser = new OSUser(username, password);
    }

    @Test
    public void runas() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(osUser, null, null);

        Process process = processBuilder.command("hostname").start();
        int exitCode = process.waitFor();

        System.out.println("[RUNAS] Process finished with exit code : " + exitCode);

        assertEquals(0, exitCode);
    }

    @Test
    public void shortPath() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(osUser, null, null);
        String tmpDir = System.getProperty("java.io.tmpdir");
        String shortPath = processBuilder.getShortPath(tmpDir);
        System.out.println("shortPath=" + shortPath);
        assertNotNull(shortPath);
    }

    @Test(timeout = 7000)
    public void runas_destroy_process() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(osUser, null, null);

        Process process = processBuilder.command("ping", "-n", "20", "localhost").start();
        process.destroy();

        System.out.println("[RUNAS_DESTROYPROCESS] Process finished with exit code : " + process.waitFor());
    }

    @Test
    public void runasEnvVar() throws Exception {
        WindowsProcessBuilder processBuilder = new WindowsProcessBuilder(osUser, null, null);

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
