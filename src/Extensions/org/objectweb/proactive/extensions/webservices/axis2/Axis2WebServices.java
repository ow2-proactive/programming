/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.webservices.axis2;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.axis2.transport.http.AxisServlet;
import org.apache.log4j.Logger;
import org.mortbay.jetty.servlet.ServletHolder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.AbstractWebServices;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.axis2.deployer.PADeployer;
import org.objectweb.proactive.extensions.webservices.axis2.util.Util;
import org.objectweb.proactive.extensions.webservices.common.MethodUtils;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * @author The ProActive Team
 *
 */
public class Axis2WebServices extends AbstractWebServices implements WebServices {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    /**
     * Add the Axis2 servlet to the jetty server and set the initial parameters.
     *
     * @throws WebServicesException
     */
    private synchronized void initializeServlet() throws WebServicesException {

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

        logger.debug("Axis servlet has been deployed on the local Jetty server " +
            "with its embedded ServiceDeployer service located at " + this.url + WSConstants.SERVICES_PATH +
            "ServiceDeployer");
    }

    /**
     * Constructor
     *
     * @param url
     * @throws WebServicesException
     */
    public Axis2WebServices(String url) throws WebServicesException {
        super(url);
        initializeServlet();
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServices#exposeAsWebService(java.lang.Object, java.lang.String, java.lang.String[])
     */
    public void exposeAsWebService(Object o, String urn, String[] methods) throws WebServicesException {
        PADeployer.deploy(o, this.url, urn, methods, false);

        logger.debug("The object of type '" + o.getClass().getSuperclass().getName() +
            "' has been deployed on " + this.url + WSConstants.SERVICES_PATH + urn + "?wsdl");
        logger.debug("Only the following methods of this object have been deployed: ");
        for (String method : methods) {
            logger.debug(" - " + method);
        }
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServices#exposeAsWebService(java.lang.Object, java.lang.String, java.lang.reflect.Method[])
     */
    public void exposeAsWebService(Object o, String urn, Method[] methods) throws WebServicesException {
        ArrayList<String> methodsName = MethodUtils.getCorrespondingMethodsName(methods);
        PADeployer.deploy(o, this.url, urn, methodsName.toArray(new String[methodsName.size()]), false);

        logger.debug("The object of type '" + o.getClass().getSuperclass().getName() +
            "' has been deployed on " + this.url + WSConstants.SERVICES_PATH + urn + "?wsdl");
        logger.debug("Only the following methods of this object have been deployed: ");
        for (Method method : methods) {
            logger.debug(" - " + method.getName());
        }
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServices#exposeAsWebService(java.lang.Object, java.lang.String)
     */
    public void exposeAsWebService(Object o, String urn) throws WebServicesException {
        PADeployer.deploy(o, this.url, urn, null, false);

        logger.debug("The object of type '" + o.getClass().getSuperclass().getName() +
            "' has been deployed on " + this.url + WSConstants.SERVICES_PATH + urn + "?wsdl");
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServices#unExposeAsWebService(java.lang.String)
     */
    public void unExposeAsWebService(String urn) throws WebServicesException {
        PADeployer.undeploy(this.url, urn);

        logger.debug("The service '" + urn + "' previously deployed on " + this.url +
            WSConstants.SERVICES_PATH + urn + "?wsdl " + "has been undeployed");
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServices#exposeComponentAsWebService(org.objectweb.fractal.api.Component, java.lang.String, java.lang.String[])
     */
    public void exposeComponentAsWebService(Component component, String componentName, String[] interfaceNames)
            throws WebServicesException {
        PADeployer.deployComponent(component, this.url, componentName, interfaceNames);

        for (String name : interfaceNames) {
            logger.debug("The component interface '" + name + "' has been deployed on " + this.url +
                WSConstants.SERVICES_PATH + componentName + "_" + name + "?wsdl");
        }
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServices#exposeComponentAsWebService(org.objectweb.fractal.api.Component, java.lang.String)
     */
    public void exposeComponentAsWebService(Component component, String componentName)
            throws WebServicesException {
        PADeployer.deployComponent(component, this.url, componentName, null);

        Object[] interfaces = component.getFcInterfaces();
        for (Object o : interfaces) {
            Interface interface_ = (Interface) o;
            String interfaceName = interface_.getFcItfName();
            if (!interfaceName.contains("-controller") && !interfaceName.equals("component") &&
                !((ProActiveInterfaceType) interface_.getFcItfType()).isFcClientItf()) {

                logger.debug("The component interface '" + interfaceName + "' has been deployed on " +
                    this.url + WSConstants.SERVICES_PATH + componentName + "_" + interfaceName + "?wsdl");
            }
        }
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServices#unExposeComponentAsWebService(org.objectweb.fractal.api.Component, java.lang.String)
     */
    public void unExposeComponentAsWebService(Component component, String componentName)
            throws WebServicesException {
        PADeployer.undeployComponent(component, this.url, componentName);

        Object[] interfaces = component.getFcInterfaces();
        for (Object o : interfaces) {
            Interface interface_ = (Interface) o;
            String interfaceName = interface_.getFcItfName();
            if (!interfaceName.contains("-controller") && !interfaceName.equals("component") &&
                !((ProActiveInterfaceType) interface_.getFcItfType()).isFcClientItf()) {
                logger.debug("The component interface '" + interfaceName + "' previously deployed on " +
                    this.url + WSConstants.SERVICES_PATH + componentName + "_" + interfaceName +
                    "?wsdl has been undeployed");
            }
        }
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServices#unExposeComponentAsWebService(java.lang.String, java.lang.String[])
     */
    public void unExposeComponentAsWebService(String componentName, String[] interfaceNames)
            throws WebServicesException {
        PADeployer.undeployComponent(this.url, componentName, interfaceNames);

        for (String name : interfaceNames) {
            logger.debug("The component interface '" + name + "' previously deployed on " + this.url +
                WSConstants.SERVICES_PATH + componentName + "_" + name + "?wsdl has been undeployed");
        }
    }

}
