/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.extensions.webservices.axis2;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Utility constants for deploying active objects and components as Web Services
 *
 * @author The ProActive Team
 */
public class WSConstants extends org.objectweb.proactive.extensions.webservices.WSConstants {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    // Path to the ProActive.jar archive
    // Used to retrieve Axis2 configuration file
    public static final String PROACTIVE_JAR;
    static {
        String temp = null;
        try {
            temp = new URI(WSConstants.class.getResource(
                    "/org/objectweb/proactive/extensions/webservices/axis2").getPath()).getPath();
        } catch (URISyntaxException use) {
            logger.error("Cannot find ProActive.jar path: " + use.getMessage(), use);
        }
        if (temp != null) {
            temp = temp.substring(0, temp.indexOf('!'));
            PROACTIVE_JAR = temp.substring(temp.indexOf(':') + 1);
        } else {
            PROACTIVE_JAR = "unknown ProActive.jar path";
        }
    }

    // Files to extract from the ProActive.jar
    public static final String AXIS_XML_ENTRY = "org/objectweb/proactive/extensions/webservices/axis2/conf/axis2.xml";
    public static final String AXIS_REPOSITORY_ENTRY = "org/objectweb/proactive/extensions/webservices/axis2/repository/";

}
