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
package org.objectweb.proactive.core.httpserver;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.xml.XmlConfiguration;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** ProActive web server
 * 
 * HTTServer allows ProActive modules to register servlets at runtime. 
 * It is based on Jetty
 */
public class HTTPServer {
    final private static Logger logger = ProActiveLogger.getLogger(Loggers.HTTP_SERVER);

    final public static String SERVER_CONTEXT = "/proactive";

    private static HTTPServer httpServer;

    /* The Jetty server */
    final private Server server;

    /* The context for ProActive */
    final private Context context;

    static synchronized public HTTPServer get() {
        if (httpServer == null) {
            try {
                httpServer = new HTTPServer();
            } catch (Exception e) {
                logger.error("HTTP Server cannot be started", e);
            }
        }

        return httpServer;
    }

    private HTTPServer() throws Exception {
        this.server = new Server();

        /*
         * Let the user decide which HTTP connector to use. Some are more efficient for few busy
         * connections, some other deals better with a lot of mostly idle connections.
         * 
         * A SelectSocketConnector is used by default
         */
        final Connector connector;
        if (CentralPAPropertyRepository.PA_HTTP_JETTY_CONNECTOR.isSet()) {
            String clName = CentralPAPropertyRepository.PA_HTTP_JETTY_CONNECTOR.getValue();
            try {
                final Class<?> cl = Class.forName(clName);
                final Class<? extends Connector> clConnector = cl.asSubclass(Connector.class);
                connector = clConnector.newInstance();
            } catch (ClassNotFoundException e) {
                logger.error("Failed to load Jetty connector " + clName);
                throw e;
            }
        } else {
            connector = new SelectChannelConnector();
        }

        /*
         * If PA_XMLHTTP_PORT is set by the user use the value. Otherwise use a random port.
         */
        int port = 0;
        if (CentralPAPropertyRepository.PA_XMLHTTP_PORT.isSet()) {
            port = CentralPAPropertyRepository.PA_XMLHTTP_PORT.getValue();
        }
        connector.setPort(port);
        this.server.addConnector(connector);

        UnboundedThreadPool utp = new UnboundedThreadPool();
        this.server.setThreadPool(utp);

        /* Lets users customize Jetty if needed */
        final URL configUrl;
        if (CentralPAPropertyRepository.PA_HTTP_JETTY_XML.isSet()) {
            final String fileLoc = CentralPAPropertyRepository.PA_HTTP_JETTY_XML.getValue();
            configUrl = new File(fileLoc).toURI().toURL();
        } else {
            configUrl = this.getClass().getResource("jetty.xml");
        }

        try {
            final XmlConfiguration configuration = new XmlConfiguration(configUrl);
            configuration.configure(server);
        } catch (Exception e) {
            logger.error("Failed to load jetty configuration file: " + configUrl, e);
        }

        this.context = new Context(server, HTTPServer.SERVER_CONTEXT, Context.SESSIONS);

        this.server.start();
        // If a random port is used we have to set it
        CentralPAPropertyRepository.PA_XMLHTTP_PORT.setValue(connector.getLocalPort());

        logger.debug("Started the HTTP server on port " + connector.getLocalPort());
    }

    /** Stop the HTTP server 
     * 
     * @throws Exception If the HTTP server fails to stop
     */
    public void stop() throws Exception {
        this.server.stop();
    }

    public boolean isMapped(String mapping) {
        ServletMapping[] servletMapping = this.context.getServletHandler().getServletMappings();
        if (servletMapping == null)
            return false;
        for (ServletMapping sm : servletMapping) {
            if (Arrays.asList(sm.getPathSpecs()).contains(mapping))
                return true;
        }
        return false;
    }

    /**
     * Register a Servlet with a given mapping
     * 
     * The servlet will be available as soon as the method complete.
     * 
     * @param servletHolder
     *            The Servlet
     * @param mapping
     *            The mapping
     */
    synchronized public void registerServlet(final ServletHolder servletHolder, final String mapping) {
        /*
         * Jetty does not allows to add Servlet once the server is started.
         * 
         * According to the Jetty developpers, we can safely use this hack if this method is
         * synchronized. We just keep a reference onto the context and add manually our Servlet into
         * the collection of ServletHandlers.
         * 
         * See: http://docs.codehaus.org/display/JETTY/Architecture
         */
        context.getServletHandler().addServletWithMapping(servletHolder, mapping);
        logger.debug("Registered servlet " + servletHolder.getClassName() + " with mapping " + mapping);
    }
}
