/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.extensions.webservices.axis2.servicedeployer;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.SchemaGenerator;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.Loader;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.extensions.webservices.common.MethodUtils;


/**
 * This class implements the service which will be deployed on the server at the
 * same time that the proactive web application. This service is used to deploy and undeploy
 * Active Object and components on the server side.
 *
 * @author The ProActive Team
 */
public class ServiceDeployer {

    static {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        try {
            RuntimeFactory.getDefaultRuntime();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
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
     * @throws Exception
     */
    private AxisService customCreateService(String implClass, String serviceName,
            AxisConfiguration axisConfiguration, ClassLoader loader, ArrayList<String> excludedOperations)
            throws Exception {

        // Create the message receivers map to be passed in the service creation
        //	in order to make the service sue our custom message receivers
        HashMap<String, MessageReceiver> messageReceiverMap = new HashMap<String, MessageReceiver>();
        Class<?> inOnlyMessageReceiver = Loader
                .loadClass("org.objectweb.proactive.extensions.webservices.axis2.receiver.PAInOnlyMessageReceiver");
        MessageReceiver messageReceiver = (MessageReceiver) inOnlyMessageReceiver.newInstance();
        messageReceiverMap.put(WSDL2Constants.MEP_URI_IN_ONLY, messageReceiver);
        Class<?> inoutMessageReceiver = Loader
                .loadClass("org.objectweb.proactive.extensions.webservices.axis2.receiver.PAInOutMessageReceiver");
        MessageReceiver inOutmessageReceiver = (MessageReceiver) inoutMessageReceiver.newInstance();
        messageReceiverMap.put(WSDL2Constants.MEP_URI_IN_OUT, inOutmessageReceiver);
        messageReceiverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY, inOutmessageReceiver);

        SchemaGenerator schemaGenerator;
        AxisService service = new AxisService();
        service.setParent(axisConfiguration);
        service.setName(serviceName);

        /**
         * Uncomment the following lines if you want the deployer to be able to handle document/literal
         * format.
         * In this case, the default DocLitBareSchemaGenerator class does not handle inherited methods as
         * it extends the DefaultSchemaGenerator class and not the CustomDefaultSchemaGenerator one.
         * N.B.: document/literal format is more restrictive. for instance, using this format, you
         * 		 cannot generate the wsdl of a class having two methods with a same argument's name.
         */
        //            Parameter generateBare = service.getParameter(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
        //            if (generateBare != null && "true".equals(generateBare.getValue())) {
        //                schemaGenerator = new DocLitBareSchemaGenerator(loader, implClass, null,
        //                    Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX, service);
        //            } else {
        schemaGenerator = new CustomDefaultSchemaGenerator(loader, implClass, null,
            Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX, service);
        //            }
        schemaGenerator.setElementFormDefault((CentralPAPropertyRepository.PA_WEBSERVICES_ELEMENTFORMDEFAULT
                .isTrue()) ? Java2WSDLConstants.FORM_DEFAULT_QUALIFIED
                : Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
        Utils.addExcludeMethods(excludedOperations);
        schemaGenerator.setExcludeMethods(excludedOperations);

        return AxisService.createService(implClass, serviceName, axisConfiguration, messageReceiverMap, null,
                loader, schemaGenerator, service);

    }

    /**
     * Expose the marshalled active object as a web service
     *
     * @param marshalledObject marshalled object
     * @param serviceName Name of the service
     * @param methods methods to be exposed
     * @param isComponent Boolean saying whether it is a component
     * @throws Exception
     */
    public void deploy(byte[] marshalledObject, String serviceName, String[] methods, boolean isComponent)
            throws Exception {

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

        // Retrieve methods we don't want
        MethodUtils mc = new MethodUtils(superclass);
        ArrayList<String> excludedOperations = mc.getExcludedMethodsName(methods);

        // Create the service
        AxisService axisService = this.customCreateService(implClass, serviceName, axisConfiguration, loader,
                excludedOperations);

        // Add the marshalled object to the service in order to be used
        // by the message receiver.
        axisService.addParameter("MarshalledObject", marshalledObject);

        // Add the variable isComponent to the service
        axisService.addParameter("isComponent", Boolean.toString(isComponent));

        // Add the service to the axis configuration
        axisConfiguration.addService(axisService);
    }

    /**
     * Undeploy the service whose name is serviceName
     *
     * @param serviceName
     * @throws AxisFault
     */
    public void undeploy(String serviceName) throws AxisFault {
        // Get the axis configuration
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        AxisConfiguration axisConfig = msgCtx.getRootContext().getAxisConfiguration();

        // Remove the service from the axis configuration
        axisConfig.removeService(serviceName);
    }
}
