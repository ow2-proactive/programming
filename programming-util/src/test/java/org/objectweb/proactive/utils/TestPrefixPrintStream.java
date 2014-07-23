/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 * TestPrefixPrintWriter
 *
 * @author The ProActive Team
 **/
public class TestPrefixPrintStream {

    @Test
    public void testPrefixPrintStream() throws IOException {
        File file = File.createTempFile("TestPrefixPrintStream", "txt");
        FileOutputStream fos = new FileOutputStream(file);
        String nl = System.getProperty("line.separator");
        String expectedResult = "AAABBB1234" + nl + "AAABBB1234" + nl + "AAABBB1234" + nl;
        PrefixPrintStream pps = new PrefixPrintStream(new PrefixPrintStream(fos, "AAA"), "BBB");
        for (int i = 0; i < 3; i++) {
            pps.print("1");
            pps.print("2");
            pps.print("3");
            pps.println("4");
        }
        pps.flush();
        pps.close();
        String output = FileUtils.readFileToString(file);
        System.out.println(output);
        Assert.assertEquals(expectedResult, output);
    }
}
