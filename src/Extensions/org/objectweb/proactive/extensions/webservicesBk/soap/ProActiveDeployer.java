/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.extensions.webservicesBk.soap;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.soap.SOAPException;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.ServiceManagerClient;
import org.apache.soap.server.TypeMapping;
import org.apache.soap.util.xml.QName;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservicesBk.WSConstants;
import org.objectweb.proactive.extensions.webservicesBk.wsdl.WSDLGenerator;


/**
 * @author The ProActive Team
 * This class is responsible to deploy an active object as a web service.
 * It serialize the stub/proxy into a string and send it to the rcprouter Servlet in order to register it on the tomcat server.
 * */
public class ProActiveDeployer extends WSConstants {

    public static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    /**
     *  Deploy an active object as a web service
     * @param urn The name of the web service
     * @param url  The web server url  where to deploy the service - typically "http://localhost:8080"
     * @param o The active Object to be deployed as a web service
     * @param methods The methods of the active object you  want to be accessible. If null, all public methods will be exposed.
     */
    public static void deploy(String urn, String url, Object o, String[] methods) {
        deploy(urn, url, o, methods, false);
    }

    /**
     *  Deploy a component as a web service. Each interface of the component will be accessible by
     * the urn <componentName>_<interfaceName> in order to identify the component an interface belongs to.
     * All the interfaces public  methods will be exposed.
     * @param componentName The name of the component
     * @param url  The web server URL  where to deploy the service - typically "http://localhost:8080"
     * @param component The component owning the interfaces that will be deployed as web services.
     */
    public static void deployComponent(String componentName, String url, Component component) {
        logger.info("deploying ws Component name  : " + componentName);

        Object[] interfaces = component.getFcInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            Interface interface_ = ((Interface) interfaces[i]);
            logger.info("interface class = " + interface_.getClass());
            /* only expose server interfaces and not the attributes controller */
            if (!(interface_.getFcItfName().contains("-controller")) &&
                !interface_.getFcItfName().equals("component")) {

                if (!((ProActiveInterfaceType) interface_.getFcItfType()).isFcClientItf()) {

                    String name = interface_.getFcItfName();
                    logger.info("exposing interface = " + name);
                    /* get all the public methods */
                    Method[] methods = interface_.getClass().getMethods();
                    Vector<String> meths = new Vector<String>();

                    for (int j = 0; j < methods.length; j++) {
                        String methodName = methods[j].getName();

                        if (isAllowedMethod(methodName)) {
                            logger.info("exposing method : " + methodName);
                            meths.addElement(methodName);
                        }
                    }

                    String[] methsArray = new String[meths.size()];
                    meths.toArray(methsArray);
                    String wsName = componentName + "_" + name;
                    logger.info("web Service Name = " + wsName);
                    deploy(wsName, url, component, methsArray, true);
                }
            }
        }

    }

    /**
     *  Undeploy a service on a web server
     * @param urn The name (urn) of the service
     * @param url The web server url
     */
    public static void undeploy(String urn, String url) {
        ServiceManagerClient serviceManagerClient = null;

        try {
            serviceManagerClient = new ServiceManagerClient(new URL(url + SERV_RPC_ROUTER));
            serviceManagerClient.undeploy(urn);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SOAPException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Undeploy component interfaces on a web server
     * @param componentName The name of the component
     * @param url The url of the web server
     * @param component The component owning the services interfaces
     */
    public static void undeployComponent(String componentName, String url, Component component) {
        Object[] interfaces = component.getFcInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            Interface inter = (Interface) interfaces[i];

            if (!(inter instanceof LifeCycleController)) {
                if (!((ProActiveInterfaceType) inter.getFcItfType()).isFcClientItf()) {
                    String serviceName = componentName + "_" + inter.getFcItfName();
                    undeploy(serviceName, url);
                }
            }
        }
    }

    /*
     * deploy services.
     */
    private static void deploy(String urn, String url, Object o, String[] methods, boolean componentInterface) {
        String wsdl;

        /* first we need to generate a WSDL description of the object we want to deploy */
        if (componentInterface) {
            logger.info("deploying interface : " + urn);
            wsdl = WSDLGenerator.getWSDL(o.getClass(), urn, url + SERV_RPC_ROUTER, DOCUMENTATION, methods);
        } else {
            wsdl = WSDLGenerator.getWSDL(o.getClass().getSuperclass(), urn, url + SERV_RPC_ROUTER,
                    DOCUMENTATION, methods);
        }

        /*For deploying an active object we need a ServiceManagerClient that will contact the Serlvet */
        ServiceManagerClient serviceManagerClient = null;

        try {
            serviceManagerClient = new ServiceManagerClient(new URL(url + SERV_RPC_ROUTER));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /* The informations about services will be put in a deployment descriptor */
        DeploymentDescriptor dd = new DeploymentDescriptor();
        dd.setID(urn);

        dd.setProviderType(DeploymentDescriptor.PROVIDER_USER_DEFINED);

        dd.setServiceClass(PROACTIVE_PROVIDER);

        dd.setIsStatic(false);

        /* Get the type mapping for customized returned types */
        TypeMapping[] tms = getMappings(o.getClass(), methods);
        dd.setMappings(tms);

        if (methods != null) {
            dd.setMethods(methods);
        } else {
            Method[] ms = o.getClass().getDeclaredMethods();
            Vector<String> mv = new Vector<String>();
            for (int i = 0; i < ms.length; i++)
                if (!disallowedMethods.contains(ms[i].getName())) {
                    mv.addElement(ms[i].getName());
                }
            methods = new String[mv.size()];
            Enumeration<String> e = mv.elements();
            int j = 0;
            while (e.hasMoreElements())
                methods[j++] = e.nextElement();

            dd.setMethods(methods);
        }
        logger.info("Deploying an object of the class :" + o.getClass());
        dd.setProviderClass(o.getClass().getName());

        /* Here we put the serialized stub into a dd property */
        Hashtable<String, Object> props = new Hashtable<String, Object>();

        props.put(WSDL_FILE, wsdl);

        if (componentInterface) {
            logger.info("INTERFACE");
            props.put(COMPONENT_INTERFACE, "true");
            props.put(PROACTIVE_STUB, HttpMarshaller.marshallObject(o));

        } else {
            logger.info("PAS INTERFACE");
            props.put(COMPONENT_INTERFACE, "false");
            props.put(PROACTIVE_STUB, HttpMarshaller.marshallObject(o));

        }

        dd.setProps(props);

        try {
            serviceManagerClient.deploy(dd);
        } catch (SOAPException e1) {
            e1.printStackTrace();
        }
    }

    /*
     * check if a method can be exposed as WS
     */
    private static boolean isAllowedMethod(String method) {
        return !disallowedMethods.contains(method);
    }

    /*
     *  Gets the types mapping for customize returned types
     * Only Java Beans are supported
     */
    private static TypeMapping[] getMappings(Class<?> c, String[] methods) {
        Vector<TypeMapping> tms = new Vector<TypeMapping>();
        Vector<String> sMethods = new Vector<String>();

        if (methods != null) {
            for (int i = 0; i < methods.length; i++) {
                sMethods.addElement(methods[i]);
            }

            Vector<Method> mMethods = new Vector<Method>();
            Method[] ms = c.getDeclaredMethods();

            for (int i = 0; i < ms.length; i++) {
                mMethods.addElement(ms[i]);
            }

            Enumeration<Method> e = mMethods.elements();

            while (e.hasMoreElements()) {
                Method m = e.nextElement();

                if (sMethods.contains(m.getName())) {
                    Class<?>[] parameters = m.getParameterTypes();

                    for (int j = 0; j < parameters.length; j++) {
                        if (!supportedTypes.contains(parameters[j])) {
                            String pname = extractName(parameters[j]);

                            TypeMapping tm = new TypeMapping("http://schemas.xmlsoap.org/soap/encoding/",
                                new QName("http://" + pname, getSimpleName(parameters[j])), parameters[j]
                                        .getName(), "org.apache.soap.encoding.soapenc.BeanSerializer",
                                "org.apache.soap.encoding.soapenc.BeanSerializer");

                            tms.addElement(tm);
                        }
                    }
                }
            }
        } else { // methods == null
            Method[] ms = c.getDeclaredMethods();

            for (int i = 0; i < ms.length; i++) {
                if (!disallowedMethods.contains(ms[i].getName())) {
                    Class<?>[] parameters = ms[i].getParameterTypes();
                    for (int j = 0; j < parameters.length; j++) {
                        if (!supportedTypes.contains(parameters[j])) {
                            String pname = extractName(parameters[j]);

                            TypeMapping tm = new TypeMapping("http://schemas.xmlsoap.org/soap/encoding/",
                                new QName("http://" + pname, getSimpleName(parameters[j])), parameters[j]
                                        .getName(), "org.apache.soap.encoding.soapenc.BeanSerializer",
                                "org.apache.soap.encoding.soapenc.BeanSerializer");

                            tms.addElement(tm);
                        }
                    }
                }
            }
        }

        TypeMapping[] tmsArray = new TypeMapping[tms.size()];
        Enumeration<TypeMapping> e = tms.elements();

        int i = 0;

        while (e.hasMoreElements()) {
            tmsArray[i++] = e.nextElement();
        }

        return tmsArray;
    }

    private static String getSimpleName(Class<?> c) {
        String cName = c.getName();
        StringTokenizer st = new StringTokenizer(cName, ".");
        String result = "";

        while (st.hasMoreTokens())
            result = st.nextToken();

        return result;
    }

    /*
     * Utility to construct the namespace of the type mapping
     */
    private static String extractName(Class<?> c) {
        String result = new String();

        Package p = c.getPackage();

        if (p != null) {
            String packageName = p.getName();
            StringTokenizer st = new StringTokenizer(packageName, ".");
            int nbTokens = st.countTokens();
            String[] tmp = new String[nbTokens];
            int n = 0;

            while (st.hasMoreTokens())
                tmp[n++] = st.nextToken();

            for (int i = nbTokens - 1; i > -1; i--)
                result += (tmp[i] + ".");

            return result.substring(0, result.length() - 1);
        }

        return "DefaultNamespace";
    }
}
