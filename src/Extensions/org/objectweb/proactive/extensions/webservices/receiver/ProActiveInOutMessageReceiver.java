package org.objectweb.proactive.extensions.webservices.receiver;

import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.rpc.receivers.RPCUtil;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.Parameter;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;
import java.lang.reflect.Method;


import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.api.PAActiveObject;

public class ProActiveInOutMessageReceiver extends AbstractInOutMessageReceiver {
	public void invokeBusinessLogic(MessageContext inMessageContext,
		MessageContext outMessageContext) throws AxisFault {
		try {
		System.out.println("Got the message ==> " +
			inMessageContext.getEnvelope().getBody().getFirstElement().toString());

		AxisService axisService = inMessageContext.getServiceContext().getAxisService();
		String registerName = (String) axisService.getParameter("RegisterName").getValue();
		String className = (String) axisService.getParameter("ServiceClass").getValue();
		
		String url = URIBuilder.buildURIFromProperties(ProActiveInet.getInstance().getHostname(), registerName).toString();
		Object activeObject = PAActiveObject.lookupActive(className, url);

		AxisOperation op = inMessageContext.getOperationContext().getAxisOperation();
		OMElement methodElement = inMessageContext.getEnvelope().getBody().getFirstElement();

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
		
		Object resObject = null;
		if (inAxisMessage != null) {
			resObject = RPCUtil.invokeServiceClass(inAxisMessage,
				method,
				activeObject,
				messageNameSpace,
				methodElement,
				inMessageContext);
		}
		SOAPFactory fac = getSOAPFactory(inMessageContext);

		// Handling the response
		AxisMessage outaxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
		if (outaxisMessage != null && outaxisMessage.getElementQName() !=null) {
			messageNameSpace = outaxisMessage.getElementQName().getNamespaceURI();
		} else {
			messageNameSpace = axisService.getTargetNamespace();
		}

		OMNamespace ns = fac.createOMNamespace(messageNameSpace,
                                               axisService.getSchemaTargetNamespacePrefix());
		SOAPEnvelope envelope = fac.getDefaultEnvelope();
		OMElement bodyContent = null;

		if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(
			op.getMessageExchangePattern())){
			OMElement bodyChild = fac.createOMElement(outMessageContext.getAxisMessage().getName(), ns);
			envelope.getBody().addChild(bodyChild);
			outMessageContext.setEnvelope(envelope);
			return;
		}
		Parameter generateBare = axisService.getParameter(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
		if (generateBare!=null && "true".equals(generateBare.getValue())) {
			RPCUtil.processResonseAsDocLitBare(resObject, axisService,
				envelope, fac, ns,
				bodyContent, outMessageContext);
		} else {
			RPCUtil.processResponseAsDocLitWrapped(resObject, axisService,
				method, envelope, fac, ns,
				bodyContent, outMessageContext);
		}
		outMessageContext.setEnvelope(envelope);

		System.out.println("returned the message ==> " +
			outMessageContext.getEnvelope().getBody().getFirstElement().toString());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
