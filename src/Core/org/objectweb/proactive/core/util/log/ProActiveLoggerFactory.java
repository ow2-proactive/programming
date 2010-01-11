/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
            MDC.put("shortid@hostname", Math.abs(UniqueID.getCurrentVMID().hashCode() % 100000) + "@" +
                getHostName());
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
