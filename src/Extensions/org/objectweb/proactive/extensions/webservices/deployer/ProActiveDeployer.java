
package org.objectweb.proactive.extensions.webservices.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.Math;


public class ProActiveDeployer implements Deployer {

	private ConfigurationContext configContext;

	public void init(ConfigurationContext configContext) {
		this.configContext = configContext;
	}

	/**
	 * Builds the xml file.
	 *
	 * @param o
	 * @param registerName
	 * @param methods
	 * @return an OMElement which represents the root of the xml tree.
	 */
	private OMElement BuildXML(Object o, String registerName, String methods[]) {
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

		ArrayList<Method> methodsArray = this.getMethods(superclass, methods);

		// operation list of tags
		Iterator<Method> itOp = methodsArray.iterator();
		OMElement operation;
		OMAttribute operationName;
		while (itOp.hasNext()) {
			Method m = itOp.next();
			operation = factory.createOMElement("operation", null);
			operationName = factory.createOMAttribute("name", null, m.getName());
			operation.addAttribute(operationName);
			service.addChild(operation);
			if (m.getReturnType().getName().equals("void")) {
				operation.addChild(opTypeInOnly.cloneOMElement());
			} else {
				operation.addChild(opTypeInOut.cloneOMElement());
			}
			OMElement actionMapping = factory.createOMElement("actionMapping", null);
			OMText actionMappingText = factory.createOMText(actionMapping, "urn:" + m.getName());
			actionMapping.addChild(actionMappingText);
			operation.addChild(actionMapping);
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
	private String chooseRegisterName(Object o) throws IOException {
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

	/**
	 * If methodsName is not null, then it returns an ArrayList of the methods of objectClass
	 * whose name is contained in methodsName.
	 * If methodsName is null, then it returns an ArrayList of all the methods of objectClass
	 *
	 * @param objectClass
	 * @param methodsName
	 * @return the ArrayList of methods to be deployed
	 */
	private ArrayList<Method> getMethods(Class<?> objectClass, String[] methodsName) {
		ArrayList<Method> methodsArray = new ArrayList<Method>();
		ArrayList<String> methodsNameArray = new ArrayList<String>();
		for (String name : methodsName) {
			methodsNameArray.add(name);
		}
		Method[] methodsTable = objectClass.getDeclaredMethods();
		boolean isEmpty = methodsNameArray.isEmpty();
		for (Method m : methodsTable) {
			if ( isEmpty || methodsNameArray.contains(m.getName())) {
				methodsArray.add(m);
			}
		}
		return methodsArray;
	}

	/**
	 * Creates the xml file used for the deployment
	 *
	 * @param o Object to deploy
	 * @param methods Methods of o to deploy
	 */
	public void deploy(Object o, String[] methods) {
		try {

			String serviceName = o.getClass().getSuperclass().getSimpleName();
			String registerName = chooseRegisterName(o);

			PAActiveObject.registerByName(o, registerName);

			// Constructs the OMElement which represents the custom services.xml file
			OMElement serviceXml = BuildXML(o, registerName, methods);

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
			// Create the xml parser
			File file = deploymentFileData.getFile();
			FileInputStream fis = new FileInputStream(file);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(fis);
			StAXOMBuilder builder = new StAXOMBuilder(reader);
			OMElement service = builder.getDocumentElement();

			// Retrieve service name
			String serviceName = service.getAttributeValue(new QName("name"));

			// Retrieve class and register name
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

			// Create the message receivers
			HashMap<String, MessageReceiver> messageReceiverMap = new HashMap<String, MessageReceiver>();
			Class inOnlyMessageReceiver = Loader
					.loadClass("org.objectweb.proactive.extensions.webservices.receiver.ProActiveInOnlyMessageReceiver");
			MessageReceiver messageReceiver = (MessageReceiver) inOnlyMessageReceiver
					.newInstance();
			messageReceiverMap.put(WSDL2Constants.MEP_URI_IN_ONLY,
					messageReceiver);
			Class inoutMessageReceiver = Loader
					.loadClass("org.objectweb.proactive.extensions.webservices.receiver.ProActiveInOutMessageReceiver");
			MessageReceiver inOutmessageReceiver = (MessageReceiver) inoutMessageReceiver
					.newInstance();
			messageReceiverMap.put(WSDL2Constants.MEP_URI_IN_OUT,
					inOutmessageReceiver);
			messageReceiverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
					inOutmessageReceiver);

			// Construct the service with all the methods of the class
			AxisService axisService = AxisService.createService(
					implClass, configContext.getAxisConfiguration(), messageReceiverMap,
				null, null, configContext.getAxisConfiguration().getSystemClassLoader());

			// Create the list of methods we want to deploy
			ArrayList<String> listOfOperation = new ArrayList<String>();
			Iterator itOperation = service.getChildrenWithName(new QName("", "operation"));
			while(itOperation.hasNext()) {
				listOfOperation.add(((OMElement) itOperation.next()).getAttributeValue(new QName("name")));
			}

			// Removed axisOperations that we don't want to be deploy
			// and axisOperations corresponding to disallowed methods
			Iterator itOp = axisService.getOperations();
			while (itOp.hasNext()) {
				AxisOperation axisOperation = (AxisOperation) itOp.next();
				String operationName = axisOperation.getName().getLocalPart();
				if (! listOfOperation.contains(operationName)
					|| WSConstants.disallowedMethods.contains(operationName))
				{
					axisService.removeOperation(axisOperation.getName());
				}
			}

			// Add the register name to be able to lookup the active object
			axisService.addParameter("RegisterName", registerName);

			// Deploy the service
			configContext.getAxisConfiguration().addService(axisService);

	        ProActiveLogger.getLogger(Loggers.WEB_SERVICES).info("Deployed the class " + implClass +
				" as a web service to http://localhost:8080/axis2/services/" + serviceName);
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
			// Unregister the service
			int slashIndex = fileName.lastIndexOf('/');
			int pointIndex = fileName.lastIndexOf('.');
			String serviceName = fileName.substring(slashIndex + 1, pointIndex);
			ProActiveLogger.getLogger(Loggers.WEB_SERVICES).info(serviceName);
			this.configContext.getAxisConfiguration().removeService(serviceName);

			// Erase the file
			File file = new File(fileName);
			if (file.exists())
			{
				ProActiveLogger.getLogger(Loggers.WEB_SERVICES).info(fileName + " has been deleted");
				file.delete();
			}
	        ProActiveLogger.getLogger(Loggers.WEB_SERVICES).info("Undeployed the service " + serviceName);
		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
		}
	}
}
