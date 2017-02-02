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
package org.objectweb.proactive.core.classloading.protocols.pamop;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.objectweb.proactive.core.classloading.protocols.AbstractHandler;
import org.objectweb.proactive.core.classloading.protocols.ProActiveConnection;
import org.objectweb.proactive.core.mop.MOPClassLoader;


public class Handler extends AbstractHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {

        String classname = u.getPath().substring(1);
        InputStream is = MOPClassLoader.getMOPClassLoader().getResourceAsStream(classname);

        return new ProActiveConnection(u, is);
    }
}
