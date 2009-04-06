package org.objectweb.proactive.extensions.webservices.receiver;

import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.rpc.receivers.RPCUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.api.PAActiveObject;

public class ProActiveInOnlyMessageReceiver extends AbstractInMessageReceiver {
	protected void invokeBusinessLogic(MessageContext inMessageContext) throws AxisFault {
		try {
			ProActiveLogger.getLogger(Loggers.WEB_SERVICES).info("Got the message ==> " +
				inMessageContext.getEnvelope().getBody().getFirstElement().toString());

			AxisService axisService = inMessageContext.getServiceContext().getAxisService();

			String registerName = (String) axisService.getParameter("RegisterName").getValue();
			String className = (String) axisService.getParameter("ServiceClass").getValue();

			String url = URIBuilder.buildURIFromProperties(ProActiveInet.getInstance().getHostname(), registerName).toString();
			Object activeObject = PAActiveObject.lookupActive(className, url);

			AxisOperation op = inMessageContext.getOperationContext().getAxisOperation();

			OMElement methodElement = inMessageContext.getEnvelope().getBody().getFirstElement();
			OMFactory factory = OMAbstractFactory.getOMFactory();
			methodElement.setNamespace(factory.createOMNamespace(axisService.getTargetNamespace(), null));

			AxisMessage inAxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

			String messageNameSpace = null;
			Method method = (Method) op.getParameterValue("myMethod");

			if (method == null) {
				String methodName = op.getName().getLocalPart();
				Method[] methods = activeObject.getClass().getSuperclass().getMethods();

				for (int i = 0; i < methods.length; i++) {
					 if (methods[i].getName().equals(methodName)) {
						  method = methods[i];
						  op.addParameter("myMethod", method);
						  break;
					 }
				}
				if (method == null) {
					 throw new AxisFault("No such method '" + methodName +
								"' in class " + className);
				}
			}

			if (inAxisMessage != null) {
				RPCUtil.invokeServiceClass(inAxisMessage,
					method,
					activeObject,
					messageNameSpace,
					methodElement,
					inMessageContext);
			}
			replicateState(inMessageContext);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
