/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.webservices.axis2;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.AbstractWebServices;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.axis2.deployer.PADeployer;
import org.objectweb.proactive.extensions.webservices.axis2.initialization.Axis2Initializer;
import org.objectweb.proactive.extensions.webservices.common.MethodUtils;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * @author The ProActive Team
 *
 */
public class Axis2WebServices extends AbstractWebServices implements WebServices {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    /**
     * Constructor
     *
     * @param url
     * @throws WebServicesException
     */
    public Axis2WebServices(String url) throws WebServicesException {
        super(url);
        Axis2Initializer.init();
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
