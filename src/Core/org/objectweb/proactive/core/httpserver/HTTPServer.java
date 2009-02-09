/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.httpserver;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;
import org.mortbay.xml.XmlConfiguration;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** ProActive web server
 * 
 * HTTServer allows ProActive modules to register servlets at runtime. 
 * It is based on Jetty
 */
public class HTTPServer {
    final private static Logger logger = ProActiveLogger.getLogger(Loggers.HTTPSERVER);

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
        if (PAProperties.PA_HTTP_JETTY_CONNECTOR.isSet()) {
            String clName = PAProperties.PA_HTTP_JETTY_CONNECTOR.getValue();
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
        if (PAProperties.PA_XMLHTTP_PORT.isSet()) {
            port = PAProperties.PA_XMLHTTP_PORT.getValueAsInt();
        }
        connector.setPort(port);
        this.server.addConnector(connector);

        UnboundedThreadPool utp = new UnboundedThreadPool();
        this.server.setThreadPool(utp);

        /* Lets users customize Jetty if needed */
        if (PAProperties.PA_HTTP_JETTY_XML.isSet()) {
            final String fileLoc = PAProperties.PA_HTTP_JETTY_XML.getValue();
            try {
                final XmlConfiguration configuration = new XmlConfiguration(fileLoc);
                configuration.configure(server);
            } catch (Exception e) {
                logger.error("Failed to load jetty configuration file", e);
            }
        }

        this.context = new Context(server, "/", Context.NO_SESSIONS);

        this.server.start();
        // If a random port is used we have to set it
        PAProperties.PA_XMLHTTP_PORT.setValue(connector.getLocalPort());

        logger.debug("Started the HTTP server on port " + connector.getLocalPort());
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
