/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.webservices.cxf.servicedeployer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.SerializableMethod;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.common.MethodUtils;


/**
 * This class implements the service which will be deployed on the server at the
 * same time as the proactive web application. This service is used to deploy and undeploy
 * Active Object and components on the server side.
 *
 * @author The ProActive Team
 */
public class ServiceDeployer implements ServiceDeployerItf {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    static {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        try {
            RuntimeFactory.getDefaultRuntime();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    // Map of servers corresponding to service servers
    // It is used to undeploy a service. In that case, we just need
    // to retrieve the corresponding Server and to stop it.
    private HashMap<String, Server> serverList = new HashMap<String, Server>();

    /**
     * Expose the marshalled active object as a web service
     *
     * @param marshalledObject marshalled object
     * @param serviceName Name of the service
     * @param marshalledSerializedMethods byte array representing the methods (of type Method)
     *        to be exposed
     * @param isComponent Specify whether the object we want to expose is a component.
     * @throws NoSuchInterfaceException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public void deploy(byte[] marshalledObject, String serviceName, byte[] marshalledSerializedMethods,
            boolean isComponent) throws NoSuchInterfaceException, ClassNotFoundException {

        Object o = HttpMarshaller.unmarshallObject(marshalledObject);
        Class<?> superclass = null;
        String implClass = null;

        ReflectionServiceFactoryBean serviceFactory = new ReflectionServiceFactoryBean();
        ServerFactoryBean svrFactory = null;

        // This method is called to force element to be unqualified
        // It is the default behaviour but if you want to expose an active
        // object using a set of methods, element forms become qualified.
        serviceFactory.setQualifyWrapperSchema(CentralPAPropertyRepository.PA_WEBSERVICES_ELEMENTFORMDEFAULT
                .isTrue());

        if (!isComponent) {
            superclass = o.getClass().getSuperclass();
            implClass = superclass.getName();

            ArrayList<SerializableMethod> serializableMethods = (ArrayList<SerializableMethod>) HttpMarshaller
                    .unmarshallObject(marshalledSerializedMethods);

            Method[] methods = MethodUtils.getMethodsFromSerializableMethods(serializableMethods);
            MethodUtils mc = new MethodUtils(superclass);
            List<Method> ignoredMethods = mc.getExcludedMethods(methods);

            serviceFactory.setIgnoredMethods(ignoredMethods);

            svrFactory = new ServerFactoryBean(serviceFactory);
            svrFactory.setServiceBean(superclass.cast(o));

        } else {
            String interfaceName = serviceName.substring(serviceName.lastIndexOf('_') + 1);
            Interface interface_;
            interface_ = (Interface) ((Component) o).getFcInterface(interfaceName);
            implClass = ((InterfaceType) interface_.getFcItfType()).getFcItfSignature();
            superclass = Class.forName(implClass, true, interface_.getClass().getClassLoader());

            svrFactory = new ServerFactoryBean(serviceFactory);
            svrFactory.setServiceBean(superclass.cast(interface_));
        }

        svrFactory.setServiceClass(superclass);
        svrFactory.setAddress("/" + serviceName);

        if (logger.getLevel() != null && logger.getLevel() == Level.DEBUG) {

            /*
             * Attaches a list of in-interceptors
             */
            List<Interceptor> inInterceptors = new ArrayList<Interceptor>();
            LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
            inInterceptors.add(loggingInInterceptor);
            svrFactory.setInInterceptors(inInterceptors);

            /*
             * Attaches a list of out-interceptors
             */
            List<Interceptor> outInterceptors = new ArrayList<Interceptor>();
            LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
            outInterceptors.add(loggingOutInterceptor);
            svrFactory.setOutInterceptors(outInterceptors);
        }

        svrFactory.create();

        serverList.put(serviceName, svrFactory.getServer());

        logger.debug("The service " + serviceName + " has been deployed");

    }

    /**
     * Undeploy the service whose name is serviceName
     *
     * @param serviceName name of the service
     */
    public void undeploy(String serviceName) {
        Server serviceServer = serverList.get(serviceName);
        serviceServer.stop();
        serverList.remove(serviceName);
        logger.debug("The service " + serviceName + " has been undeployed");
    }
}
