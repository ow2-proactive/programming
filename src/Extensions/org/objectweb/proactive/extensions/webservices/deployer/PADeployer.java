package org.objectweb.proactive.extensions.webservices.deployer;

import java.lang.reflect.Method;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.apache.log4j.Logger;


public class PADeployer {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    /**
     * Call the method deploy of the ServiceDeployer service
     * deployed on the host we want to deploy our active object.
     *
     * @param o Active object or component we want to deploy
     * @param url Url of the host
     * @param urn Name of the service
     * @param methods Methods to be deployed
     * @param isComponent Boolean saying whether it is a component
     */
    static public void deploy(Object o, String url, String urn, String[] methods, boolean isComponent) {
        try {
            RPCServiceClient serviceClient = new RPCServiceClient();
            EndpointReference targetEPR = new EndpointReference(url + WSConstants.AXIS_SERVICES_PATH +
                "ServiceDeployer");

            Options options = serviceClient.getOptions();
            options.setTo(targetEPR);

            QName op = new QName("http://servicedeployer.webservices.extensions.proactive.objectweb.org",
                "deploy");

            Object[] opArgs = new Object[] { HttpMarshaller.marshallObject(o), urn, methods, isComponent };

            serviceClient.invokeRobust(op, opArgs);

            logger.info("Called the deployer service to deploy " + urn + " to " + url);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    /**
     * Check if a method can be exposed as WS
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
    * @param interfacesName Names of the interfaces we want to deploy.
     * 						 	 If null, then all the interfaces will be deployed
     */
    static public void deployComponent(Component component, String url, String componentName,
            String[] interfaceNames) {

        Object[] interfaces;
        if (interfaceNames == null) {
            interfaces = component.getFcInterfaces();
            logger.info("Deploying all interfaces of " + componentName);
        } else {
            interfaces = new Object[interfaceNames.length];
            for (int i = 0; i < interfaceNames.length; i++) {
                try {
                    logger.info("Deploying the interface" + interfaceNames[i] + " of " + componentName);
                    interfaces[i] = component.getFcInterface(interfaceNames[i]);
                } catch (NoSuchInterfaceException e) {
                    logger.error("Impossible to retrieve the interface whose name is " + interfaceNames[i]);
                    logger.error("Retrieve all interfaces");
                    interfaces = component.getFcInterfaces();
                    break;
                }
            }
        }

        for (int i = 0; i < interfaces.length; i++) {
            Interface interface_ = ((Interface) interfaces[i]);

            /* only expose server interfaces and not the attributes controller */
            if (!(interface_.getFcItfName().contains("-controller")) &&
                !interface_.getFcItfName().equals("component")) {

                if (!((ProActiveInterfaceType) interface_.getFcItfType()).isFcClientItf()) {

                    String name = interface_.getFcItfName();

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
                }
            }
        }

    }

    /**
     * Call the method unDeploy of the ServiceDeployer service
     * deployed on the host.
     *
     * @param url Url of the host where the service is deployed
     * @param serviceName Name of the service.
     */
    static public void unDeploy(String url, String serviceName) {
        try {
            RPCServiceClient serviceClient = new RPCServiceClient();
            EndpointReference targetEPR = new EndpointReference(url + WSConstants.AXIS_SERVICES_PATH +
                "DeployerService");

            Options options = serviceClient.getOptions();
            options.setTo(targetEPR);

            QName op = new QName("http://servicedeployer.webservices.extensions.proactive.objectweb.org",
                "unDeploy");

            Object[] opArgs = new Object[] { serviceName };

            serviceClient.invokeRobust(op, opArgs);

            logger.info("Called the deployer to " + url + " to undeploy the " + serviceName + "service");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    /**
     * Call the method unDeploy of the ServiceDeployer service
     * deployed on the host for each interface.
     *
     * @param component Component to undeploy
     * @param url Url of the host where interfaces are deployed
     * @param componentName Name of the component
     * @param interfaceNames Interfaces we want to undeploy.
      * 							  If null, then all the interfaces will be undeployed
     */
    static public void unDeployComponent(Component component, String url, String componentName,
            String[] interfaceNames) {
        if (interfaceNames.length == 0) {
            Object[] interfaces = component.getFcInterfaces();
            for (Object o : interfaces) {
                unDeploy(url, componentName + "_" + ((Interface) o).getFcItfName());
            }
        } else {
            for (String s : interfaceNames) {
                unDeploy(url, componentName + "_" + s);
            }
        }
    }

}
