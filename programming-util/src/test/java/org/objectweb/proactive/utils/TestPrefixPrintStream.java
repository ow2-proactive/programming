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
