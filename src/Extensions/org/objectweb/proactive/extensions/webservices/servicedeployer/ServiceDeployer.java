package org.objectweb.proactive.extensions.webservices.servicedeployer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.util.Loader;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator;
import org.apache.axis2.description.java2wsdl.DocLitBareSchemaGenerator;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.SchemaGenerator;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 *
 * This class implements the service which will be deployed on the server at the
 * same time that the proactive web application. This service is used to deploy and undeploy
 * Active Object and components on the server side.
 */
public class ServiceDeployer {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    static {
        System.setSecurityManager(new java.rmi.RMISecurityManager());
        try {
            RuntimeFactory.getDefaultRuntime();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the methods to be excluded. These methods are methods defined in the
     * WSConstants.disallowedMethods vector and methods which are not in methodsName.
     * In case of a null methodsName, only methods in dissallowdMethods vector are
     * returned.
     *
     * @param objectClass
     * @param methodsName
     * @return
     */
    private ArrayList<String> getExcludedOperations(Class<?> objectClass, String[] methodsName) {
        ArrayList<String> excludedOperations = new ArrayList<String>();

        Iterator<String> it = WSConstants.disallowedMethods.iterator();

        while (it.hasNext()) {
            excludedOperations.add(it.next());
        }

        if (methodsName.length == 0)
            return excludedOperations;

        Method[] methodsTable = objectClass.getDeclaredMethods();

        ArrayList<String> methodsNameArray = new ArrayList<String>();
        for (String name : methodsName) {
            methodsNameArray.add(name);
        }

        for (Method m : methodsTable) {
            if (!methodsNameArray.contains(m.getName())) {
                excludedOperations.add(m.getName());
            }
        }

        return excludedOperations;
    }

    /**
     * Custom creation of service. This enables to change the name of the service
     * and to select methods to be exposed, since these functionalities are not available
     * from current version of Axis2.
     *
     * @param implClass
     * @param serviceName
     * @param axisConfiguration
     * @param loader
     * @param excludedOperations
     * @return
     */
    private AxisService customCreateService(String implClass, String serviceName,
            AxisConfiguration axisConfiguration, ClassLoader loader, ArrayList<String> excludedOperations,
            String[] methods) {

        try {
            // Create the message receivers map to be passed in the service creation
            //	in order to make the service sue our custom message receivers
            HashMap<String, MessageReceiver> messageReceiverMap = new HashMap<String, MessageReceiver>();
            Class<?> inOnlyMessageReceiver = Loader
                    .loadClass("org.objectweb.proactive.extensions.webservices.receiver.PAInOnlyMessageReceiver");
            MessageReceiver messageReceiver = (MessageReceiver) inOnlyMessageReceiver.newInstance();
            messageReceiverMap.put(WSDL2Constants.MEP_URI_IN_ONLY, messageReceiver);
            Class<?> inoutMessageReceiver = Loader
                    .loadClass("org.objectweb.proactive.extensions.webservices.receiver.PAInOutMessageReceiver");
            MessageReceiver inOutmessageReceiver = (MessageReceiver) inoutMessageReceiver.newInstance();
            messageReceiverMap.put(WSDL2Constants.MEP_URI_IN_OUT, inOutmessageReceiver);
            messageReceiverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY, inOutmessageReceiver);

            SchemaGenerator schemaGenerator;
            AxisService service = new AxisService();
            service.setParent(axisConfiguration);
            service.setName(serviceName);

            Parameter generateBare = service.getParameter(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
            if (generateBare != null && "true".equals(generateBare.getValue())) {
                schemaGenerator = new DocLitBareSchemaGenerator(loader, implClass, null,
                    Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX, service);
            } else {
                schemaGenerator = new DefaultSchemaGenerator(loader, implClass, null,
                    Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX, service);
            }
            schemaGenerator.setElementFormDefault(Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
            Utils.addExcludeMethods(excludedOperations);
            schemaGenerator.setExcludeMethods(excludedOperations);

            return AxisService.createService(implClass, serviceName, axisConfiguration, messageReceiverMap,
                    null, loader, schemaGenerator, service);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
      * Expose the marshalled active object as a web service
     *
     * @param marshalledObject marshalled object
      * @param serviceName Name of the service
     * @param methods methods to be exposed
      * @param isComponent Boolean saying whether it is a component
     */
    public void deploy(byte[] marshalledObject, String serviceName, String[] methods, boolean isComponent) {
        try {

            // Retrieve the axis configuration to enable to deploy a service
            MessageContext msgCtx = MessageContext.getCurrentMessageContext();
            AxisConfiguration axisConfiguration = msgCtx.getRootContext().getAxisConfiguration();

            // Get original class and its name
            Class<?> superclass;
            String implClass;
            Object o = null;
            Object component = null;
            ClassLoader loader = null;
            if (!isComponent) {
                // Unmarshalled object
                o = HttpMarshaller.unmarshallObject(marshalledObject);
                superclass = o.getClass().getSuperclass();
                loader = o.getClass().getClassLoader();
                implClass = superclass.getName();
            } else {
                // Unmarshalled object
                component = HttpMarshaller.unmarshallObject(marshalledObject);
                String interfaceName = serviceName.substring(serviceName.lastIndexOf('_') + 1);
                Interface interface_ = (Interface) ((Component) component).getFcInterface(interfaceName);
                superclass = interface_.getClass();
                loader = superclass.getClassLoader();
                implClass = ((InterfaceType) interface_.getFcItfType()).getFcItfSignature();
            }
            System.out.println(implClass);

            // Retrieve methods we don't want
            ArrayList<String> excludedOperations = this.getExcludedOperations(superclass, methods);

            // Create the service
            AxisService axisService = this.customCreateService(implClass, serviceName, axisConfiguration,
                    loader, excludedOperations, methods);

            // Add the marshalled object to the service in order to be used
            // by the message receiver.
            axisService.addParameter("MarshalledObject", marshalledObject);

            // Add the variable isComponent to the service
            axisService.addParameter("isComponent", Boolean.toString(isComponent));

            // Add the service to the axis configuration
            axisConfiguration.addService(axisService);
            logger.info("The deployer service has deployed the service " + serviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
      * Undeploy the service whose name is serviceName
     *
     * @param serviceName
     */
    public void unDeploy(String serviceName) {
        try {
            // Get the axis configuration
            MessageContext msgCtx = MessageContext.getCurrentMessageContext();
            AxisConfiguration axisConfig = msgCtx.getRootContext().getAxisConfiguration();

            // Remove the service from the axis configuration
            axisConfig.removeService(serviceName);
            logger.info("The deployer service has undeployed the service " + serviceName);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
    }
}
