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
package org.objectweb.proactive.extensions.webservices.soap;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Response;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.RPCRouter;
import org.apache.soap.util.Provider;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.WSConstants;


/**
 * @author The ProActive Team
 * This class is responsible to locate an active object deployed as a web service and invoke a method on this object.
 */
public class ProActiveProvider extends WSConstants implements Provider {

    static {
        System.setSecurityManager(new java.rmi.RMISecurityManager());
        try {
            RuntimeFactory.getDefaultRuntime();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    private static Logger logger = logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    private DeploymentDescriptor dd;
    private Envelope envelope;
    private Call call;
    private String methodName;
    private String targetObjectURI;
    private HttpServlet servlet;
    private HttpSession session;
    private Object targetObject;

    /**
     * This method is responsible for locating the active object.
     * First, we make a lookup active in order to retrieve the active object and then store it in the private field targetObject
     * @see org.apache.soap.util.Provider
     */
    public void locate(DeploymentDescriptor dd, Envelope env, Call call, String methodName,
            String targetObjectURI, SOAPContext reqContext) throws SOAPException {
        //Set some properties to the context
        HttpServlet servlet = (HttpServlet) reqContext.getProperty(Constants.BAG_HTTPSERVLET);
        HttpSession session = (HttpSession) reqContext.getProperty(Constants.BAG_HTTPSESSION);

        if (logger.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append("================================================");
            sb.append("In ProActiveProvider.locate()");
            sb.append("Method: " + methodName);
            sb.append("URI: " + targetObjectURI);
            sb.append("DD.ServiceClass: " + dd.getServiceClass());
            sb.append("DD.ProviderClass: " + dd.getProviderClass());
            sb.append("Call.MethodName: " + call.getMethodName());
            logger.debug(sb);
        }

        @SuppressWarnings("unchecked")
        Hashtable props = dd.getProps();
        String className = dd.getProviderClass();

        this.dd = dd;
        this.envelope = env;
        this.call = call;
        this.methodName = methodName;
        this.targetObjectURI = targetObjectURI;
        this.servlet = servlet;
        this.session = session;

        if (!RPCRouter.validCall(dd, call)) {
            System.err.println("It's not a valid call");

            SOAPException e = new SOAPException(Constants.FAULT_CODE_CLIENT, "It's not a  valid call");
            throw e;
        }

        byte[] serObj = (byte[]) props.get("Stub");

        boolean isInterfaceComponent = ((String) props.get(WSConstants.COMPONENT_INTERFACE)).equals("true");
        try {
            if (!isInterfaceComponent) {
                logger.debug("Normal object");
                targetObject = HttpMarshaller.unmarshallObject(serObj);
            } else {
                logger.debug("target object is a component interface");
                Object component = HttpMarshaller.unmarshallObject(serObj);
                logger.debug("component class :" + component.getClass());

                String actualName = targetObjectURI.substring(targetObjectURI.lastIndexOf('_') + 1);

                targetObject = ((ProActiveComponentRepresentative) component).getFcInterface(actualName);
                logger.debug("target Object class  = " + targetObject.getClass());
                Class<?> c = targetObject.getClass();
                Method[] meths = c.getMethods();

                if (logger.isDebugEnabled()) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < meths.length; i++) {
                        sb.append(meths[i] + ",");
                    }
                    sb.append("\ntarget object superclass : " + targetObject.getClass().getSuperclass());
                    logger.debug(sb);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     *  This method is responsible to invoke the method on the targetObject
     * First We invoke the method thanks to RPCRouter.invoke() method then we build an enveloppe that contains the response.
     * @see org.apache.soap.util.Provider
     **/
    public void invoke(SOAPContext reqContext, SOAPContext resContext) throws SOAPException {
        //  System.out.println("targetObject = " + targetObject);

        //dd.setProviderClass(targetObject.getClass().getName());
        // Add logic to invoke the service and get back the result here
        try {
            //	System.out.println("target Object = " + targetObject);
            Response resp = RPCRouter.invoke(dd, call, targetObject, reqContext, resContext);
            //build the enveloppe that contains the response
            Envelope env = resp.buildEnvelope();
            //            System.out.println(env);
            StringWriter sw = new StringWriter();
            env.marshall(sw, call.getSOAPMappingRegistry(), resContext);
            resContext.setRootPart(sw.toString(), Constants.HEADERVAL_CONTENT_TYPE_UTF8);
        } catch (Exception e) {
            logger.debug("--- >exception ! " + e.getMessage() + " -- " + e.getClass().getName(), e);
            SOAPException ex = new SOAPException(Constants.FAULT_CODE_SERVER, e.getMessage());
            System.out.println("An error has occured when trying to invoke the method on the object");
            throw ex;
        }
    }
}
