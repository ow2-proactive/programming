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

import org.apache.axis2.transport.http.AxisServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.deployer.PADeployer;


/**
 * @author The ProActive Team
 *
 * Deploy and undeploy Active Object and components. Methods of this class
 * just call methods of the PADeployer class.
 */
@PublicAPI
public final class WebServices extends WSConstants {

    static {

        // Retrieve or launch a Jetty server
        // in case of a local exposition
        HTTPServer httpServer = HTTPServer.get();

        // Create an Axis servlet
        ServletHolder axisServletHolder = new ServletHolder(new AxisServlet());

        // Define axis2 configuration file and repository where services and modules
        // are stored. The repository path is mandatory since it contains the ServiceDeployer
        // service which is used to expose our active objects as webservice.
        axisServletHolder.setInitParameter("axis2.xml.path", WSConstants.AXIS_XML_PATH);
        axisServletHolder.setInitParameter("axis2.repository.path", WSConstants.AXIS_REPOSITORY_DIR);

        // Register the Axis Servlet to Jetty
        httpServer.registerServlet(axisServletHolder, WSConstants.AXIS_SERVLET);

        ProActiveLogger.getLogger(Loggers.WEB_SERVICES).info(
                "Deployed axis servlet on the local Jetty server");
    }

    /**
     * Expose an active object as a web service
     *
     * @param o The object to expose as a web service
     * @param url The url of the host where the object will be deployed  (typically http://localhost:8080)
     * @param urn The name of the object
     * @param methods The methods that will be exposed as web services functionalities
     *					 If null, then all methods will be exposed
     */
    public static void exposeAsWebService(Object o, String url, String urn, String[] methods) {
        PADeployer.deploy(o, url, urn, methods, false);
    }

    /**
     * Expose an active object with all its methods as a web service
     *
     * @param o The object to expose as a web service
     * @param url The url of the host where the object will be deployed  (typically http://localhost:8080)
     * @param urn The name of the object
     */
    public static void exposeAsWebService(Object o, String url, String urn) {
        PADeployer.deploy(o, url, urn, null, false);
    }

    /**
     * Delete the service on a web server
      *
     * @param urn The name of the object
     * @param url The url of the web server
     */
    public static void unExposeAsWebService(String url, String urn) {
        PADeployer.unDeploy(url, urn);
    }

    /**
     * Expose a component as web service. Each server and controller
     * interface of the component will be accessible by  the urn
     * [componentName]_[interfaceName]in order to identify the component an
     * interface belongs to.
     * Only the interfaces public methods of the specified interfaces will be exposed.
     *
     * @param component The component owning the interfaces that will be deployed as web services.
     * @param url  Web server url  where to deploy the service - typically "http://localhost:8080"
     * @param componentName Name of the component
     * @param interfacesName Names of the interfaces we want to deploy.
      *							  If null, then all the interfaces will be deployed
     */
    public static void exposeComponentAsWebService(Component component, String url, String componentName,
            String[] interfacesName) {
        PADeployer.deployComponent(component, url, componentName, interfacesName);
    }

    /**
     * Expose a component as web service. Each server and controller
     * interface of the component will be accessible by  the urn
     * [componentName]_[interfaceName]in order to identify the component an
     * interface belongs to.
     * All the interfaces public methods of all interfaces will be exposed.
     *
     * @param component The component owning the interfaces that will be deployed as web services.
     * @param url  Web server url  where to deploy the service - typically "http://localhost:8080"
     * @param componentName Name of the component
     */
    public static void exposeComponentAsWebService(Component component, String url, String componentName) {
        PADeployer.deployComponent(component, url, componentName, null);
    }

    /**
     * Undeploy all the interfaces of a component deployed on a web server
     *
     * @param component  The component owning the services interfaces
     * @param url The url of the web server
     * @param componentName The name of the component
     */
    public static void unExposeComponentAsWebService(Component component, String url, String componentName) {
        PADeployer.unDeployComponent(component, url, componentName);
    }

    /**
     * Undeploy specified interfaces of a component deployed on a web server
     *
     * @param url The url of the web server
     * @param componentName The name of the component
     * @param interfaceNames Interfaces to be undeployed
     */
    public static void unExposeComponentAsWebService(String url, String componentName, String[] interfaceNames) {
        PADeployer.unDeployComponent(url, componentName, interfaceNames);
    }
}
