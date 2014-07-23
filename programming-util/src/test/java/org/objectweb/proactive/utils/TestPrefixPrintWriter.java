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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;


/**
 * TestPrefixPrintWriter
 *
 * @author The ProActive Team
 **/
public class TestPrefixPrintWriter {

    @Test
    public void testPrefixPrintWriter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String nl = System.getProperty("line.separator");
        String expectedResult = "AAABBB1234" + nl + "AAABBB1234" + nl + "AAABBB1234" + nl;
        PrefixPrintWriter ppw = new PrefixPrintWriter(new PrefixPrintWriter(pw, "AAA"), "BBB");
        for (int i = 0; i < 3; i++) {
            ppw.print("1");
            ppw.print("2");
            ppw.print("3");
            ppw.println("4");
        }
        ppw.flush();
        String output = sw.toString();
        System.out.println(output);
        Assert.assertEquals(expectedResult, output);
    }
}
