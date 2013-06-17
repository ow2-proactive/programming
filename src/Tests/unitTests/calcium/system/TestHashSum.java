/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package unitTests.calcium.system;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;

import functionalTests.TestDisabler;


public class TestHashSum {

    @BeforeClass
    public static void beforeClass() {
        TestDisabler.unsupportedOs(OperatingSystem.windows);
    }

    @Test
    public void TestSha1Sum() throws Exception {
        String shakespeare = "If music be the food of love, play on\n"
            + "Give me excess of it, that, surfeiting,\n" + "The appetite may sicken, and so die.";

        File testfile = new File(System.getProperty("java.io.tmpdir"), "test-calcium-hashsum-shakespeare");

        if (testfile.exists()) {
            testfile.delete();
        }

        assertFalse(testfile.exists());

        PrintWriter out = new PrintWriter(new FileWriter(testfile));
        out.println(shakespeare);
        out.close();

        assertTrue(testfile.exists());

        String hexStringHash = org.objectweb.proactive.extensions.calcium.system.HashSum.hashsum(testfile,
                "SHA-1");
        assertTrue(hexStringHash.equals("404d69b17da9a666fe8db79eec8483d94a43babc"));

        testfile.delete();

        assertFalse(testfile.exists());
    }
}
