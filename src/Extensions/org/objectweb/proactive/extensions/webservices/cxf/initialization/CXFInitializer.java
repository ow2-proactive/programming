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
package org.objectweb.proactive.extensions.webservices.cxf.initialization;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mortbay.jetty.servlet.ServletHolder;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.cxf.WSConstants;
import org.objectweb.proactive.extensions.webservices.cxf.servicedeployer.ServiceDeployer;
import org.objectweb.proactive.extensions.webservices.cxf.servicedeployer.ServiceDeployerItf;


public class CXFInitializer {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    public static synchronized void init() {
        // Retrieve or launch a Jetty server
        // in case of a local exposition
        HTTPServer httpServer = HTTPServer.get();

        if (httpServer.isMapped(WSConstants.SERVLET_PATH)) {
            logger.info("The CXF servlet has already been installed");
            return;
        }

        // Creates a CXF servlet and register it
        // to the Jetty server
        CXFServlet cxf = new CXFServlet();
        ServletHolder CXFServletHolder = new ServletHolder(cxf);

        httpServer.registerServlet(CXFServletHolder, WSConstants.SERVLET_PATH);

        // Configures the bus
        Bus bus = cxf.getBus();
        BusFactory.setDefaultBus(bus);

        /*
         * Configure the service
         */
        ServerFactoryBean svrFactory = new ServerFactoryBean();
        svrFactory.setAddress("/ServiceDeployer");
        svrFactory.setServiceClass(ServiceDeployerItf.class);
        svrFactory.setServiceBean(new ServiceDeployer());

        if (logger.getLevel() != null && logger.getLevel() == Level.DEBUG) {

            /*
             * Attaches in-interceptors
             * In our case, only a logger is attached in order to be able
             * to see input soap messages
             */
            LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
            svrFactory.getInInterceptors().add(loggingInInterceptor);

            /*
             * Attaches out-interceptors
             * In our case, only a logger is attached in order to be able
             * to see output soap messages
             */
            LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
            svrFactory.getOutInterceptors().add(loggingOutInterceptor);
        }

        // Creates the service
        svrFactory.create();
    }

}
