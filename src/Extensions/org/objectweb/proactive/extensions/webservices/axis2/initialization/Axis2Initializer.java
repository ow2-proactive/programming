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
package org.objectweb.proactive.extensions.webservices.axis2.initialization;

import java.io.File;

import org.apache.axis2.transport.http.AxisServlet;
import org.apache.log4j.Logger;
import org.mortbay.jetty.servlet.ServletHolder;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.axis2.WSConstants;
import org.objectweb.proactive.extensions.webservices.axis2.util.Util;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


public class Axis2Initializer {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    public static synchronized void init() throws WebServicesException {

        // Retrieve or launch a Jetty server
        // in case of a local exposition
        HTTPServer httpServer = HTTPServer.get();

        if (httpServer.isMapped(WSConstants.SERVLET_PATH))
            return;

        // Create an Axis servlet
        AxisServlet axisServlet = new AxisServlet();

        ServletHolder axisServletHolder = new ServletHolder(axisServlet);

        String tempDir = System.getProperty("java.io.tmpdir");

        // Extracts the axis2.xml file from the proactive.jar archive and return its path
        String axis2XML = Util.extractFromJar(WSConstants.PROACTIVE_JAR, WSConstants.AXIS_XML_ENTRY, tempDir,
                true);
        axisServletHolder.setInitParameter("axis2.xml.path", axis2XML);

        // Extracts the axis2 repository from the proactive.jar archive and return its path
        String axis2Repo = Util.extractFromJar(WSConstants.PROACTIVE_JAR, WSConstants.AXIS_REPOSITORY_ENTRY,
                tempDir, true);
        axisServletHolder.setInitParameter("axis2.repository.path", axis2Repo);

        // Register the Axis Servlet to Jetty
        httpServer.registerServlet(axisServletHolder, WSConstants.SERVLET_PATH);

        // Erases the _axis2 directory created by axis2 when used by jetty
        logger.debug("Erasing temporary files created by axis2 servlet...");
        File f = new File((File) axisServlet.getServletContext()
                .getAttribute("javax.servlet.context.tempdir"), "_axis2");
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (File child : files) {
                if (child.delete()) {
                    logger.debug("   - " + child.getAbsolutePath() + " has been deleted");
                } else {
                    logger.debug("   - " + child.getAbsolutePath() + " has not been deleted");
                }
            }

            if (f.delete()) {
                logger.debug("   - " + f.getAbsolutePath() + " has been deleted");
            } else {
                logger.debug("   - " + f.getAbsolutePath() + " has not been deleted");
            }
        }
    }
}
