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
package org.objectweb.proactive.core.util;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;


/**
 *
 * unit test for the URIBuilder
 *
 */
public class URITest {
    @Test
    public void checkURI() throws Exception {
        String protocol = "pnp";
        String userInfo = "machine";
        String host = "localhost.localdomain";
        String path = "apath";
        String query = "toto=value";
        int port = 1258;

        URI uri = URIBuilder.buildURI(userInfo, host, path, protocol, port, query, false);

        // checking getters
        assertTrue(URIBuilder.getPortNumber(uri) == port);

        assertTrue(host.equals(URIBuilder.getHostNameFromUrl(uri)));

        assertTrue(path.equals(URIBuilder.getNameFromURI(uri)));

        assertTrue(userInfo.equals(uri.getUserInfo()));

        assertTrue(query.equals(uri.getQuery()));

        // check the remove protocol method
        URI u = URIBuilder.removeProtocol(uri);
        assertTrue("//machine@localhost.localdomain:1258/apath?toto=value".equals(u.toString()));

        // check the remove query method
        u = URIBuilder.removeQuery(uri);
        assertTrue("pnp://machine@localhost.localdomain:1258/apath".equals(u.toString()));

        // check the setPort method
        int port2 = 5656;
        u = URIBuilder.setPort(uri, port2);
        assertTrue(port2 == u.getPort());

        // check the setProtocol
        u = URIBuilder.setProtocol(uri, "http");
        assertTrue("http://machine@localhost.localdomain:1258/apath?toto=value".equals(u.toString()));

        // validate an URI
        try {
            // wrong protocol
            URIBuilder.checkURI("://localh/s");
            assertTrue(false);
        } catch (URISyntaxException e) {
            assertTrue(true);
        }
    }
}
