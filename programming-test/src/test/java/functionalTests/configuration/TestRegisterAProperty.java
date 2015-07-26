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
package functionalTests.configuration;

import java.util.List;
import java.util.Map;

import org.junit.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.PAProperty;
import org.objectweb.proactive.core.config.PAPropertyBoolean;
import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAPropertyString;


public class TestRegisterAProperty {

    @Test
    public void test() {
        PAProperties.getAllProperties();

        PAProperties.register(MyRepository.class);

        Map<Class<?>, List<PAProperty>> map = PAProperties.getAllProperties();

        List<PAProperty> list = map.get(MyRepository.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
    }

    static class MyRepository {
        static PAPropertyBoolean myBool = new PAPropertyBoolean("myBool", false);
        static public PAPropertyString myString = new PAPropertyString("myString", false);
        static public PAPropertyInteger myInt = new PAPropertyInteger("myInt", false);
        PAPropertyBoolean nonStatic = new PAPropertyBoolean("nonStatic", false);
    }
}
