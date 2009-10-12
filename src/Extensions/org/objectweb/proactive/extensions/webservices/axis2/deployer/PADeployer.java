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
package org.objectweb.proactive.extensions.webservices.axis2.deployer;

import java.lang.reflect.Method;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.axis2.WSConstants;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * This class is in charge of calling the ServiceDeployer service on hosts specified
 * by urls and invokes its deploy and undeploy methods with the needed arguments.
 *
 * @author The ProActive Team
 */
public class PADeployer {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    /**
     * Call the method deploy of the ServiceDeployer service
     * deployed on the host we want to deploy our active object.
     *
     * @param o Active object or component we want to deploy
     * @param url Url of the host
     * @param urn Name of the service
     * @param methods Methods to be deployed
     * @param isComponent Boolean saying whether it is a component
     * @throws WebServicesException
     */
    static public void deploy(Object o, String url, String urn, String[] methods, boolean isComponent)
            throws WebServicesException {
        try {

            RPCServiceClient serviceClient = new RPCServiceClient();
            EndpointReference targetEPR = new EndpointReference(url + WSConstants.SERVICES_PATH +
                "ServiceDeployer");

            Options options = serviceClient.getOptions();
            options.setTo(targetEPR);
            options.setAction("deploy");

            QName op = new QName(
                "http://servicedeployer.axis2.webservices.extensions.proactive.objectweb.org", "deploy");

            Object[] opArgs = new Object[] { HttpMarshaller.marshallObject(o), urn, methods, isComponent };

            serviceClient.invokeRobust(op, opArgs);

        } catch (AxisFault axisFault) {
            throw new WebServicesException("An AxisFault occured when trying to deploy the service " + urn +
                " on " + url, axisFault);
        }
    }

    /**
     * Check if a method can be exposed as a web service
     *
     * @param method Name of the method
     * @return
     */
    private static boolean isAllowedMethod(String method) {
        return !WSConstants.disallowedMethods.contains(method);
    }

    /**
     * Deploy a component. This method retrieve interfaces we want to deploy as well as their methods
     * and call the method deploy.
     *
     * @param component Component to be deployed
     * @param url Url of the host
     * @param componentName Name of the component
     * @param interfaceNames Names of the interfaces we want to deploy.
     * 						 	 If null, then all the interfaces will be deployed
     * @throws WebServicesException
     */
    static public void deployComponent(Component component, String url, String componentName,
            String[] interfaceNames) throws WebServicesException {

        Object[] interfaces;
        boolean deployAllInterfaces = interfaceNames == null || interfaceNames.length == 0;
        if (deployAllInterfaces) {
            interfaces = component.getFcInterfaces();
            logger.debug("Deploying all interfaces of " + componentName);
        } else {
            interfaces = new Object[interfaceNames.length];
            for (int i = 0; i < interfaceNames.length; i++) {
                try {
                    interfaces[i] = component.getFcInterface(interfaceNames[i]);
                    logger.debug("Deploying the interface " + interfaceNames[i] + " of " + componentName);
                } catch (NoSuchInterfaceException e) {
                    throw new WebServicesException("Impossible to retrieve the interface whose name is " +
                        interfaceNames[i], e);
                }
            }
        }

        for (int i = 0; i < interfaces.length; i++) {
            Interface interface_ = ((Interface) interfaces[i]);
            String name = interface_.getFcItfName();

            /* only expose server interfaces and not the attributes controller */
            if (!(interface_.getFcItfName().contains("-controller")) &&
                !interface_.getFcItfName().equals("component") &&
                !((ProActiveInterfaceType) interface_.getFcItfType()).isFcClientItf()) {

                Method[] methods = interface_.getClass().getMethods();
                Vector<String> meths = new Vector<String>();

                for (int j = 0; j < methods.length; j++) {
                    String methodName = methods[j].getName();

                    if (isAllowedMethod(methodName)) {
                        meths.addElement(methodName);
                    }
                }

                String[] methsArray = new String[meths.size()];
                meths.toArray(methsArray);
                String wsName = componentName + "_" + name;

                deploy(component, url, wsName, methsArray, true);

            } else if (!deployAllInterfaces) {
                logger.error("The interface '" + name + "' is not a valid interface:");
                logger.error("Only the non-controller server interfaces can be exposed as a web service.");
            }
        }

    }

    /**
     * Call the method undeploy of the ServiceDeployer service
     * deployed on the host.
     *
     * @param url Url of the host where the service is deployed
     * @param serviceName Name of the service.
     * @throws WebServicesException
     */
    static public void undeploy(String url, String serviceName) throws WebServicesException {
        try {

            RPCServiceClient serviceClient = new RPCServiceClient();
            EndpointReference targetEPR = new EndpointReference(url + WSConstants.SERVICES_PATH +
                "ServiceDeployer");

            Options options = serviceClient.getOptions();
            options.setTo(targetEPR);
            options.setAction("undeploy");

            QName op = new QName(
                "http://servicedeployer.axis2.webservices.extensions.proactive.objectweb.org", "undeploy");

            Object[] opArgs = new Object[] { serviceName };

            serviceClient.invokeRobust(op, opArgs);

        } catch (AxisFault axisFault) {
            throw new WebServicesException("An AxisFault occured when trying to undeploy the service " +
                serviceName + " on " + url, axisFault);
        }
    }

    /**
     * Call the method undeploy of the ServiceDeployer service
     * deployed on the host for every interface of component.
     *
     * @param component Component to undeploy
     * @param url Url of the host where interfaces are deployed
     * @param componentName Name of the component
     * @throws WebServicesException
     */
    static public void undeployComponent(Component component, String url, String componentName)
            throws WebServicesException {
        Object[] interfaces = component.getFcInterfaces();
        for (Object o : interfaces) {
            String interfaceName = ((Interface) o).getFcItfName();

            /* only expose server interfaces and not the attributes controller */
            if (!interfaceName.contains("-controller") && !interfaceName.equals("component") &&
                !((ProActiveInterfaceType) ((Interface) o).getFcItfType()).isFcClientItf())
                undeploy(url, componentName + "_" + interfaceName);
        }
    }

    /**
     * Call the method undeploy of the ServiceDeployer service
     * deployed on the host for interfaces of component specified in
     * interfaceNames.
     *
     * @param url Url of the host where interfaces are deployed
     * @param componentName Name of the component
     * @param interfaceNames Interfaces we want to undeploy.
     * @throws WebServicesException
     */
    static public void undeployComponent(String url, String componentName, String[] interfaceNames)
            throws WebServicesException {
        for (String s : interfaceNames) {
            undeploy(url, componentName + "_" + s);
        }
    }
}
