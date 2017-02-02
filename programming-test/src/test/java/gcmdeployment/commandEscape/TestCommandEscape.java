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
package gcmdeployment.commandEscape;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.objectweb.proactive.utils.OperatingSystem;

import functionalTests.TestDisabler;


public class TestCommandEscape {
    final static String sshLocalhost = "ssh localhost";

    final static String cTrue = "true *";

    final static int vTrue = 0;

    final static String cFalse = "false \"plop\"";

    final static int vFalse = 1;

    @Before
    final public void disable() {
        TestDisabler.unsupportedOs(OperatingSystem.windows);
    }

    @Test
    public void testCommandEscape() throws IOException, InterruptedException {
        String cmdT = cTrue;
        String cmdF = cFalse;

        Assert.assertTrue(exec(cTrue) == vTrue);
        Assert.assertTrue(exec(cFalse) == vFalse);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        Assert.assertTrue(exec(cTrue) == vTrue);
        Assert.assertTrue(exec(cFalse) == vFalse);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        Assert.assertTrue(exec(cTrue) == vTrue);
        Assert.assertTrue(exec(cFalse) == vFalse);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        Assert.assertTrue(exec(cTrue) == vTrue);
        Assert.assertTrue(exec(cFalse) == vFalse);
    }

    static private String concat(String prefixCmd, String cmd) {
        return prefixCmd + " " + Helpers.escapeCommand(cmd);
    }

    static private int exec(String cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        return p.exitValue();
    }
}
