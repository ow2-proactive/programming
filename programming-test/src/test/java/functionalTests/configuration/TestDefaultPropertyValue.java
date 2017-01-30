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
package functionalTests.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.PAPropertyBoolean;
import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAPropertyString;


public class TestDefaultPropertyValue {

    @Test
    public void test() {
        PAProperties.register(MyRepository.class);

        PAPropertyBoolean pab = (PAPropertyBoolean) PAProperties.getProperty(MyRepository.myBool.getName());
        Assert.assertEquals(true, pab.getValue());

        PAPropertyString pas = (PAPropertyString) PAProperties.getProperty(MyRepository.myString.getName());
        Assert.assertEquals("toto", pas.getValue());

        PAPropertyInteger pai = (PAPropertyInteger) PAProperties.getProperty(MyRepository.myInt.getName());
        Assert.assertEquals(12, pai.getValue());
    }

    static class MyRepository {
        static PAPropertyBoolean myBool = new PAPropertyBoolean("myBool", false, true);

        static public PAPropertyString myString = new PAPropertyString("myString", false, "toto");

        static public PAPropertyInteger myInt = new PAPropertyInteger("myInt", false, 12);
    }
}
