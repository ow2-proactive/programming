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
package org.objectweb.proactive.core.classloading.protocols;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ProActiveConnection extends URLConnection {

    final static private Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);

    //    final private byte[] bytes;
    final private InputStream is;

    public ProActiveConnection(URL url) {
        this(url, (InputStream) null);
    }

    public ProActiveConnection(URL url, byte[] bytes) {
        super(url);
        this.is = new ByteArrayInputStream(bytes);
    }

    public ProActiveConnection(URL url, InputStream is) {
        super(url);
        this.is = is;
    }

    @Override
    public void connect() throws IOException {
        // DO NOTHING
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (this.is != null) {
            return is;
        } else {
            throw new IOException("This method must not be called when bytes is null");
        }
    }

}
