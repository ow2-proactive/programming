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
package org.objectweb.proactive.extensions.webservices.axis2.receiver;

import java.lang.reflect.Method;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.rpc.receivers.RPCUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.axis2.WSConstants;


/**
 * When ServiceDeployer creates a service for an active object or a component,
 * it specifies a custom message receiver which is in charge of unmarshalling
 * the object representing the service and to invoke the asked method.
 * This class implements this custom message receiver for in-only methods and
 * is strongly based on the RPCInOnlyMessageReceiver class of axis2.
 *
 * @author The ProActive Team
 */
public class PAInOnlyMessageReceiver extends AbstractInMessageReceiver {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    /**
      * This method is a adaptation of the invokeBusinessLogic method of the class RPCInOnlyMessageReceiver
      * {@inheritDoc}
      * @param inMessageContext
      * @see org.apache.axis2.receivers.AbstractMessageReceiver#invokeBusinessLogic(MessageContext)
      * @throws AxisFault
      */
    protected void invokeBusinessLogic(MessageContext inMessageContext) throws AxisFault {
        try {
            // Display the received message to be treated
            logger.debug("Got the message ==> " + inMessageContext.getEnvelope().toString());

            // Get the axis service corresponding to this call
            AxisService axisService = inMessageContext.getServiceContext().getAxisService();

            String className = (String) axisService.getParameter("ServiceClass").getValue();

            // Retrieve the isComponent parameter
            boolean isComponent = "true".equals((String) axisService.getParameter("isComponent").getValue());

            // Unmarshall the serialized object stored in a parameter of the service
            byte[] marshallObject = (byte[]) axisService.getParameter("MarshalledObject").getValue();

            Object targetObject = null;
            if (isComponent) {
                Object component = HttpMarshaller.unmarshallObject(marshallObject);

                // Extract the interface name form the address
                String address = inMessageContext.getTo().getAddress();
                int firstIndex = address.lastIndexOf(WSConstants.SERVICES_PATH);
                firstIndex += WSConstants.SERVICES_PATH.length();
                String serviceName = address.substring(firstIndex);

                int pointIndex = serviceName.indexOf('.');
                if (pointIndex != -1) {
                    serviceName = serviceName.substring(0, pointIndex);
                }

                int lastIndex = serviceName.indexOf('/');

                if (lastIndex != -1) {
                    serviceName = serviceName.substring(0, lastIndex);
                }
                String actualName = serviceName.substring(serviceName.lastIndexOf('_') + 1);

                // Get the interface
                targetObject = ((ProActiveComponentRepresentative) component).getFcInterface(actualName);
            } else {
                targetObject = HttpMarshaller.unmarshallObject(marshallObject);
            }

            // Retrieve the asked operation
            AxisOperation op = inMessageContext.getOperationContext().getAxisOperation();

            // Retrieve the good namespace from the service and give this value to the
            // namespace of the method element in the message context
            OMElement methodElement = inMessageContext.getEnvelope().getBody().getFirstElement();
            if (methodElement != null) {
                OMFactory factory = OMAbstractFactory.getOMFactory();
                methodElement.setNamespace(factory.createOMNamespace(axisService.getTargetNamespace(), null));
            }

            AxisMessage inAxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

            String messageNameSpace = null;
            Method method = (Method) op.getParameterValue("myMethod");

            if (method == null) {
                String methodName = op.getName().getLocalPart();
                Method[] methods;
                if (isComponent) {
                    methods = targetObject.getClass().getMethods();
                } else {
                    methods = targetObject.getClass().getSuperclass().getMethods();
                }
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals(methodName)) {
                        method = methods[i];
                        op.addParameter("myMethod", method);
                        break;
                    }
                }
                if (method == null) {
                    throw new AxisFault("No such method '" + methodName + "' in class " + className);
                }
            }

            // Invoke the method
            if (inAxisMessage != null) {
                RPCUtil.invokeServiceClass(inAxisMessage, method, targetObject, messageNameSpace,
                        methodElement, inMessageContext);
            }
            replicateState(inMessageContext);
        } catch (Exception e) {
            throw new AxisFault("An exception occured while treating the following message:\n" +
                inMessageContext.getEnvelope().toString(), e);
        }
    }
}
