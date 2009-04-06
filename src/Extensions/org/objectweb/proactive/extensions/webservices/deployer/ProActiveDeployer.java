
package org.objectweb.proactive.extensions.webservices.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.webservices.WSConstants;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.namespace.QName;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.Math;


public class ProActiveDeployer implements Deployer {

	private ConfigurationContext configContext;

	public void init(ConfigurationContext configContext) {
		this.configContext = configContext;
	}

	private OMElement BuildXML(Object o, String registerName) {
		Class<?> superclass = o.getClass().getSuperclass();

		OMFactory factory = OMAbstractFactory.getOMFactory();

		// service tag
		OMElement service = factory.createOMElement("service",null);
		OMAttribute serviceName = factory.createOMAttribute("name", null, superclass.getSimpleName());
		service.addAttribute(serviceName);

		// description tag
		OMElement description = factory.createOMElement("description", null);
		OMText descriptionText = factory.createOMText(description, "This is the service which exposes the class " + superclass.getName());
		description.addChild(descriptionText);
		service.addChild(description);

		// parameter tag
		OMElement parameter = factory.createOMElement("parameter", null);
		OMAttribute parameterName = factory.createOMAttribute("name", null, "ServiceClass");
		parameter.addAttribute(parameterName);
		OMText parameterText = factory.createOMText(parameter, superclass.getName());
		parameter.addChild(parameterText);
		service.addChild(parameter);


		// InOnly operation type
		OMElement opTypeInOnly = factory.createOMElement("type", null);
		OMText opTypeInOnlyText = factory.createOMText(opTypeInOnly, "InOnly");
		opTypeInOnly.addChild(opTypeInOnlyText);

		// InOut operation type
		OMElement opTypeInOut = factory.createOMElement("type", null);
		OMText opTypeInOutText = factory.createOMText(opTypeInOut, "InOut");
		opTypeInOut.addChild(opTypeInOutText);

		// operation list of tags
		Method[] methodList = superclass.getDeclaredMethods();
		OMElement operation[] = new OMElement[methodList.length];
		OMAttribute operationName[] = new OMAttribute[methodList.length];
		for(int i = 0 ; i < methodList.length ; i++ ) {
			operation[i] = factory.createOMElement("operation", null);
			operationName[i] = factory.createOMAttribute("name", null, methodList[i].getName());
			operation[i].addAttribute(operationName[i]);
			service.addChild(operation[i]);
			if (methodList[i].getReturnType().getName().equals("void")) {
				operation[i].addChild(opTypeInOnly.cloneOMElement());
			} else {
				operation[i].addChild(opTypeInOut.cloneOMElement());
			}
			OMElement actionMapping = factory.createOMElement("actionMapping", null);
			OMText actionMappingText = factory.createOMText(actionMapping, "urn:" + methodList[i].getName());
			actionMapping.addChild(actionMappingText);
			operation[i].addChild(actionMapping);
		}

		// register name
		OMElement parameter2 = factory.createOMElement("parameter", null);
		OMAttribute parameterName2 = factory.createOMAttribute("name", null, "RegisterName");
		parameter2.addAttribute(parameterName2);
		OMText registerNameText = factory.createOMText(parameter2, registerName);
		parameter2.addChild(registerNameText);
		service.addChild(parameter2);

		return service;
	}

	/**
	 * Returns an unused register name for the object o.
	 * Returned name will be of the form o.getClass().getSuperclass() + random number
	 *
	 * @param o
	 * @return A free name for registration
	 * @throws IOException
	 * 		Occurs if the list of registration names cannot be retrieved.
	 */
	private static String chooseRegisterName(Object o) throws IOException {
		String url = "http://localhost:8080";
		try {
			String registerName = o.getClass().getSuperclass().getSimpleName() + Math.round(Math.random());
			String[] list = PAActiveObject.listActive(url);
			boolean exists;
			do {
				exists = false;
				for (int i = 0; i < list.length; i++) {
					if (list[i].equals(registerName)) {
						registerName = o.getClass().getSuperclass().getSimpleName() + Math.round(Math.random());
						exists = true;
						break;
					}
				}
			} while (exists);
			return registerName;
		} catch (IOException e) {
			throw new IOException("Cannot retrieve the list of active object registered" +
					"at the url " + url );
		}
	}


	public void deploy(Object o, String[] methods) {
		try {
			String serviceName = o.getClass().getSuperclass().getSimpleName();
			String registerName = chooseRegisterName(o);

			PAActiveObject.registerByName(o, registerName);

			// Constructs the OMElement which represents the custom services.xml file
			OMElement serviceXml = BuildXML(o, registerName);

			// Writes this element into a file with a .pa extension
			FileWriter fstream =
				new FileWriter(WSConstants.AXIS_REPOSITORY_PATH + "services/" + serviceName + ".pa");
			BufferedWriter out = new BufferedWriter(fstream);
			XMLStreamWriter xmlStreamWriter =
				StAXUtils.createXMLStreamWriter(out);
			serviceXml.serialize(xmlStreamWriter);
			out.close();

			File file = new File(WSConstants.AXIS_REPOSITORY_PATH + "services/" + serviceName + ".pa");
			DeploymentFileData dfd = new DeploymentFileData(file);
			this.deploy(dfd);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
		try {
			File file = deploymentFileData.getFile();
			FileInputStream fis = new FileInputStream(file);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(fis);
			StAXOMBuilder builder = new StAXOMBuilder(reader);
			OMElement service = builder.getDocumentElement();

			String implClass = "" ;
			String registerName = "" ;
			Iterator itParam = service.getChildrenWithName(new QName("", "parameter"));
			while(itParam.hasNext()) {
				OMElement param = (OMElement) itParam.next();
				if (param.getAttributeValue(new QName("", "name")).equals("ServiceClass")) {
					implClass = param.getText();
				} else if (param.getAttributeValue(new QName("", "name")).equals("RegisterName")) {
					registerName = param.getText();
				}
			}

			HashMap<String, MessageReceiver> messageReceiverMap = new HashMap<String, MessageReceiver>();
			Class inOnlyMessageReceiver = Loader
					.loadClass("org.objectweb.proactive.extensions.webservices.receiver.ProActiveInOnlyMessageReceiver");
			MessageReceiver messageReceiver = (MessageReceiver) inOnlyMessageReceiver
					.newInstance();
			messageReceiverMap.put(WSDL2Constants.MEP_URI_IN_ONLY,
					messageReceiver);
			messageReceiverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
					messageReceiver);
			Class inoutMessageReceiver = Loader
					.loadClass("org.objectweb.proactive.extensions.webservices.receiver.ProActiveInOutMessageReceiver");
			MessageReceiver inOutmessageReceiver = (MessageReceiver) inoutMessageReceiver
					.newInstance();
			messageReceiverMap.put(WSDL2Constants.MEP_URI_IN_OUT,
					inOutmessageReceiver);

			AxisService axisService = AxisService.createService(
					implClass, configContext.getAxisConfiguration(), messageReceiverMap,
				null, null, configContext.getAxisConfiguration().getSystemClassLoader());
			axisService.addParameter("RegisterName", registerName);

			configContext.getAxisConfiguration().addService(axisService);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDirectory(String directory) {
		// if you need to know the subdirectory that the deployer has registered
	}

	public void setExtension(String extension) {
		//extension of the file that the deployer has registered
	}

	public void unDeploy(String fileName) {
		// remove all the runtime data you have created for this file
		try {
			int slashIndex = fileName.lastIndexOf('/');
			int pointIndex = fileName.lastIndexOf('.');
			String serviceName = fileName.substring(slashIndex + 1, pointIndex);
			this.configContext.getAxisConfiguration().removeService(serviceName);
		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
		}
	}
}
