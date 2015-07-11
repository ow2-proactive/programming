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
package gcmdeployment.listGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.ListGenerator;
import org.junit.Assert;
import org.junit.Test;


public class TestListGenerator {
    final static private URL validResource = TestListGenerator.class.getResource("data.valid.txt");
    final static private URL invalidResource = TestListGenerator.class.getResource("data.invalid.txt");

    /*
     * @Test public void singleTest() { ListGenerator.generateNames(""); }
     */
    @Test
    public void testValid() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(validResource.toURI())));

        while (true) {
            String question = br.readLine();
            String response = br.readLine();
            br.readLine(); // Empty line

            if (question == null) { // End of File
                break;
            }

            if (response == null) {
                throw new IllegalArgumentException("Illegal format for a data file: " + question);
            }

            Assert.assertEquals("question=\"" + question + "\"", response,
                    concat(ListGenerator.generateNames(question)));
        }
    }

    @Test
    public void testInvalid() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(invalidResource.toURI())));

        while (true) {
            String question = br.readLine();
            br.readLine(); // Empty line

            if (question == null) { // End of File
                break;
            }

            try {
                List<String> ret = ListGenerator.generateNames(question);
                Assert.fail("Question=" + question + "\" response=\"" + concat(ret) + "\"");
            } catch (IllegalArgumentException e) {
                // An IllegalArguementException is expected
            }
        }
    }

    static private String concat(List<String> lstr) {
        String ret = "";
        for (String str : lstr)
            ret += (str + " ");

        if (ret.length() > 1) {
            ret = ret.substring(0, ret.length() - 1);
        }

        return ret;
    }
}
