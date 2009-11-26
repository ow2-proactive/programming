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
