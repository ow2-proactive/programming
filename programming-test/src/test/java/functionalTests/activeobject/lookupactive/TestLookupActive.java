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
package functionalTests.activeobject.lookupactive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;

import functionalTests.FunctionalTest;


/**
 * Test register and lookup AOs
 */

public class TestLookupActive extends FunctionalTest {

    @Test
    public void action() throws Exception {
        A a = PAActiveObject.newActive(A.class, new Object[] { "toto" });
        String url = a.register();

        a = PAActiveObject.lookupActive(A.class, url);

        assertTrue(a != null);
        assertEquals(a.getName(), "toto");

        // check listActive contains the previous lookup
        String host = URIBuilder.buildURIFromProperties(ProActiveInet.getInstance().getHostname(), "").toString();
        String[] registered = PAActiveObject.listActive(url);
        assertNotNull(registered);

        for (int i = 0; i < registered.length; i++) {
            if (registered[i].substring(registered[i].lastIndexOf('/')).equals("/A")) {
                return;
            }
        }

        throw new Exception("Could not find registered object in list of objects");
    }

    @Test(expected = IOException.class)
    public void lookupNode() throws Exception {
        A a = PAActiveObject.newActive(A.class, new Object[] { "toto" });
        String nodeURL = PAActiveObject.getActiveObjectNodeUrl(a);
        PAActiveObject.lookupActive(A.class, nodeURL);
    }
}
