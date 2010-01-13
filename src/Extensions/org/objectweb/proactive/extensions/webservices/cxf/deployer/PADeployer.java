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
package org.objectweb.proactive.extensions.webservices.cxf.deployer;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.util.SerializableMethod;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.common.MethodUtils;
import org.objectweb.proactive.extensions.webservices.cxf.servicedeployer.ServiceDeployerItf;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * This class is in charge of calling the ServiceDeployer service on hosts specified
 * by urls and invokes its deploy and undeploy methods with the needed arguments.
 *
 * @author The ProActive Team
 */
public final class PADeployer {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    /**
     * Creates a client for the ServiceDeployer service
     *
     * @param url URL where the service is located
     * @return A client of type ServiceDeployerItf
     */
    private static ServiceDeployerItf getClient(String url) {
        ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
        factory.setServiceClass(ServiceDeployerItf.class);
        factory.setAddress(url + WSConstants.SERVICES_PATH + "ServiceDeployer");
        ServiceDeployerItf client = (ServiceDeployerItf) factory.create();
        return client;
    }

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
    public static void deploy(Object o, String url, String urn, Method[] methods, boolean isComponent)
            throws WebServicesException {

        byte[] marshalledObject = HttpMarshaller.marshallObject(o);
        ServiceDeployerItf client = getClient(url);
        ArrayList<SerializableMethod> serializableMethods = MethodUtils.getSerializableMethods(methods);
        // TO DO: test without serializing arraylist
        byte[] marshalledSerializedMethods = HttpMarshaller.marshallObject(serializableMethods);
        try {
            client.deploy(marshalledObject, urn, marshalledSerializedMethods, isComponent);
        } catch (Exception e) {
            throw new WebServicesException("An exception occured while trying to call the " +
                " ServiceDeployer located at " + url, e);
        }
    }

    /**
     * Call the method undeploy of the ServiceDeployer service
     * deployed on the host.
     *
     * @param url URL of the host where the service is deployed
     * @param urn Name of the service.
     */
    public static void undeploy(String url, String urn) {
        ServiceDeployerItf client = getClient(url);
        client.undeploy(urn);
    }

    /**
     * Deploy a component. This method retrieve interfaces we want to deploy as well as their methods
     * and call the method deploy.
     *
     * @param component Component to be deployed
     * @param url Url of the host
     * @param componentName Name of the component
     * @param interfaceNames Names of the interfaces we want to deploy.
     *                           If null, then all the interfaces will be deployed
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
                    logger.debug("Deploying the interface " + interfaceNames[i] + " of " + componentName);
                    interfaces[i] = component.getFcInterface(interfaceNames[i]);
                } catch (NoSuchInterfaceException e) {
                    throw new WebServicesException("Impossible to retrieve the interface whose name is " +
                        interfaceNames[i], e);
                }
            }
        }

        for (int i = 0; i < interfaces.length; i++) {
            Interface interface_ = ((Interface) interfaces[i]);
            String interfaceName = interface_.getFcItfName();

            /* only expose server interfaces and not the attributes controller */
            if (!interfaceName.contains("-controller") && !interfaceName.equals("component") &&
                !((ProActiveInterfaceType) interface_.getFcItfType()).isFcClientItf()) {

                String wsName = componentName + "_" + interfaceName;
                deploy(component, url, wsName, null, true);

            } else if (!deployAllInterfaces) {
                logger.error("The interface '" + interfaceName + "' is not a valid interface:");
                logger.error("Only the non-controller server interfaces can be exposed as a web service.");
            }
        }
    }

    /**
     * Call the method undeploy of the ServiceDeployer service
     * deployed on the host for every interface of component.
     *
     * @param component Component to undeploy
     * @param url Url of the host where interfaces are deployed
     * @param componentName Name of the component
     */
    static public void undeployComponent(Component component, String url, String componentName) {
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
     */
    static public void undeployComponent(String url, String componentName, String[] interfaceNames) {
        for (String s : interfaceNames) {
            undeploy(url, componentName + "_" + s);
        }
    }
}
