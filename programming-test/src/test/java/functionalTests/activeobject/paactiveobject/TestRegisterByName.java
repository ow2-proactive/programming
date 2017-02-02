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
package functionalTests.activeobject.paactiveobject;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;

import functionalTests.FunctionalTest;


public class TestRegisterByName extends FunctionalTest {

    public TestRegisterByName() {
    }

    /*
     * Check that PAActiveObject.registerByName returns the right URL
     * See PROACTIVE-741
     */
    @Test
    public void test() throws ProActiveException {
        AO ao = PAActiveObject.newActive(AO.class, new Object[] {});

        String name = null;
        String url = null;

        name = "!";
        url = PAActiveObject.registerByName(ao, name, false);
        Assert.assertTrue(url.endsWith(name));

        name = "aaaaaa";
        url = PAActiveObject.registerByName(ao, name, false);
        Assert.assertTrue(url.endsWith(name));

        name = "zzzzzzz";
        url = PAActiveObject.registerByName(ao, name, false);
        Assert.assertTrue(url.endsWith(name));

        name = "~";
        url = PAActiveObject.registerByName(ao, name, false);
        Assert.assertTrue(url.endsWith(name));
    }

    static public class AO {
        public AO() {
        }
    }
}
