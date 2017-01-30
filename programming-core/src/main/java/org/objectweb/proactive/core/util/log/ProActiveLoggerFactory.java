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
package org.objectweb.proactive.core.util.log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggerFactory;
import org.objectweb.proactive.core.UniqueID;


/**
 * Factory class for the loggers used within ProActive
 */
public class ProActiveLoggerFactory implements LoggerFactory {

    /**
     * Creates a new ProActiveLogger with Diagnostic Context information (hostname and runtime).
     * @see org.apache.log4j.spi.LoggerFactory#makeNewLoggerInstance(java.lang.String)
     */
    public ProActiveLogger makeNewLoggerInstance(String name) {
        if (MDC.get("hostname") == null) {
            MDC.put("hostname", getHostName());
        }

        if (MDC.get("id@hostname") == null) {
            MDC.put("id@hostname", UniqueID.getCurrentVMID() + "@" + getHostName());
        }

        if (MDC.get("shortid@hostname") == null) {
            MDC.put("shortid@hostname", Math.abs(UniqueID.getCurrentVMID().hashCode() % 100000) + "@" + getHostName());
        }

        if (MDC.get("runtime") == null) {
            MDC.put("runtime", "unknown runtime");
        }

        return new ProActiveLogger(name);
    }

    private static String getHostName() {
        try {
            // ProActiveInet.getInstance().getLocal() cannot be used here since loggers are used in
            // ProActiveConfiguration constructor.
            // It should not be that important. AFAIK this method is only used to 
            // print a hostname in the logs
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown host";
        }
    }
}
