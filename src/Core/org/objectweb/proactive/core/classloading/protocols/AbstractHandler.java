/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.classloading.protocols;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class AbstractHandler extends URLStreamHandler {
    final static private Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        // WARNING: This method will break if URL mapping change
        //
        // URL must follow this convention:
        //   paproto://host[:port]/RUNTIME/pkg/classname.class
        //

        final String runtimeUrl;
        final String classname;
        final String uStr = u.toString();

        int index = 0;
        int count = 0;
        // Find the 4th / (aka the / just after RUNTIME)
        while (count != 4 && index < uStr.length()) {
            if (uStr.charAt(index) == '/') {
                count++;
            }

            index++;
        }

        if (count == 4) {
            // It is safe to skip the first to entry since the protocol handler
            // mechanism guarantee that the protocol is correct
            runtimeUrl = uStr.substring(2, index - 1);
        } else {
            throw new IOException("Unsupported URL: " + u);
        }

        if (index == uStr.length()) {
            classname = null;
        } else {
            if ((uStr.length() - index) <= ".class".length()) {
                throw new IOException("Unsupported URL: " + u);
            } else {
                classname = uStr.substring(index, uStr.length() - ".class".length()).replace('/', '.');
            }

        }

        if (classname != null) {
            try {
                ProActiveRuntime rt = RuntimeFactory.getRuntime(runtimeUrl);
                byte[] b = rt.getClassData(classname);

                if (b != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Succeffully downloaded " + classname + " from " + runtimeUrl);
                    }
                    return new ProActiveConnection(u, b);
                } else {
                    logger.info("Failed to download " + classname + " from " + runtimeUrl);
                    throw new IOException("Failed to download " + classname + " from " + runtimeUrl);
                }
            } catch (ProActiveException e) {
                logger.info("Failed to download " + classname + " from " + runtimeUrl);
                throw new IOException("Failed to download " + classname + " from " + runtimeUrl);
            }
        } else {
            return new ProActiveConnection(u);
        }

    }
}
