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
package org.objectweb.proactive.examples.osgi.hello;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class HelloLogInfo implements HelloService {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private final String message = "Hello World!";
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /** ProActive compulsory no-args constructor */
    public HelloLogInfo() {
    }

    public void sayHello() {
        logger.info(this.message + "\n from " + getHostName() + "\n at " +
            dateFormat.format(new java.util.Date()));
    }

    /** finds the name of the local machine */
    static String getHostName() {
        return ProActiveInet.getInstance().getInetAddress().getHostName();
    }

    public void saySomething(String something) {
        logger.info(something + "\n from " + getHostName() + "\n at " +
            dateFormat.format(new java.util.Date()));
    }
}
