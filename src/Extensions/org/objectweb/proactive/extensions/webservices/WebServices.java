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
package org.objectweb.proactive.extensions.webservices;

//import java.io.File;
//import java.io.IOException;

import org.mortbay.jetty.servlet.ServletHolder;
//import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.deployer.ProActiveDeployer;
import org.objectweb.proactive.extensions.webservices.servlet.PAAxisServlet;
import org.apache.axis2.context.ConfigurationContext;

@PublicAPI
public final class WebServices {

	static ConfigurationContext configContext;
	static ProActiveDeployer deployer = new ProActiveDeployer();

    static {
        HTTPServer httpServer = HTTPServer.get();

        PAAxisServlet axisServlet = new PAAxisServlet();

		ServletHolder axisServletHolder = new ServletHolder(axisServlet);
		axisServletHolder.setInitParameter("axis2.xml.path", WSConstants.AXIS_XML_PATH);
		axisServletHolder.setInitParameter("axis2.repository.path", WSConstants.AXIS_REPOSITORY_PATH);
		httpServer.registerServlet(axisServletHolder, WSConstants.AXIS_SERVLET);

		configContext = axisServlet.getConfigContext();
        deployer.init(configContext);

//		ProActiveLogger.getLogger(Loggers.WEB_SERVICES).warn(
//				"Failed to create a temporary Service Store, " + System.getProperty("user.dir") +
//				"/DeployedService.ds will be used", e);


        ProActiveLogger.getLogger(Loggers.WEB_SERVICES).info("Deployed axis servlet");

    }

    /**
     * Expose an active object as a web service
     * @param o The object to expose as a web service
     * @param url The url of the host where the object will be deployed  (typically http://localhost:8080)
     * @param urn The name of the object
     * @param methods The methods that will be exposed as web services functionalities
     */
    public static void exposeAsWebService(Object o, String[] methods) {
        deployer.deploy(o, methods);
    }

    /**
     * Delete the service on a web server
     * @param urn The name of the object
     * @param url The url of the web server
     */
    public static void unExposeAsWebService(Object o) {
        deployer.unDeploy(WSConstants.AXIS_REPOSITORY_PATH + "services/"
			+ o.getClass().getSuperclass() + ".pa");
    }

    /**
     * Expose a component as webservice. Each server and controller
     * interface of the component will be accessible by  the urn
     * [componentName]_[interfaceName]in order to identify the component an
     * interface belongs to.
     * All the interfaces public methods will be exposed.
     *
     * @param componentName The name of the component
     * @param url  The web server url  where to deploy the service - typically "http://localhost:8080"
     * @param component The component owning the interfaces that will be deployed as web services.
     */
//    public static void exposeComponentAsWebService(Component component, String url, String componentName) {
//        ProActiveDeployer.deployComponent(componentName, url, component);
//    }

    /**
     * Undeploy component interfaces on a web server
     * @param componentName The name of the component
     * @param url The url of the web server
     * @param component  The component owning the services interfaces
     */
//    public static void unExposeComponentAsWebService(String componentName, String url, Component component) {
//        ProActiveDeployer.undeployComponent(componentName, url, component);
//    }
}
