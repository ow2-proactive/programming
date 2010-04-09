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
package org.objectweb.proactive.core.component.group;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.ProActiveInterfaceImpl;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.gen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeImpl;
import org.objectweb.proactive.core.component.representative.ProActiveNFComponentRepresentativeImpl;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.InvalidProxyClassException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A class for creating groups of interfaces
 * Indeed, the standard mechanism cannot be used here, as we are referencing components
 * through interfaces of component representatives.
 *
 * @author The ProActive Team
 */
public class ProActiveComponentGroup {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    /**
     * creates an empty group able to contain ProActiveInterfaceImpl objects of the given type..
     * The stub in front of the group proxy is of type ProActiveInterfaceImpl.
     * @param interfaceType the type of interface we need a group of Interface objects on
     * @return a group of ProActiveInterfaceImpl elements
     * @throws ClassNotFoundException
     * @throws ClassNotReifiableException
     */
    public static ProActiveInterface newComponentInterfaceGroup(ProActiveInterfaceType interfaceType,
            Component owner) throws ClassNotFoundException, ClassNotReifiableException {
        try {
            Object result = MOP.newInstance(ProActiveInterfaceImpl.class.getName(), null, null,
                    ProxyForComponentInterfaceGroup.class.getName(), null);

            ProxyForComponentInterfaceGroup proxy = (ProxyForComponentInterfaceGroup) ((StubObject) result)
                    .getProxy();
            proxy.setClassName(Interface.class.getName());

            //return a reference on the generated interface reference corresponding to the interface type
            ProActiveInterface generated = RepresentativeInterfaceClassGenerator.instance()
                    .generateFunctionalInterface(interfaceType.getFcItfName(), owner, interfaceType);
            ((StubObject) generated).setProxy(proxy);

            proxy.setInterfaceType(interfaceType);
            proxy.setOwner(owner);

            return generated;
        } catch (InvalidProxyClassException e) {
            logger.error("**** InvalidProxyClassException ****", e);
        } catch (ConstructionOfProxyObjectFailedException e) {
            logger.error("**** ConstructionOfProxyObjectFailedException ****", e);
        } catch (ConstructionOfReifiedObjectFailedException e) {
            logger.error("**** ConstructionOfReifiedObjectFailedException ****", e);
        } catch (InterfaceGenerationFailedException e) {
            logger.error("**** Interface could not be generated **** ", e);
        }
        return null;
    }

    /**
     * Creates an empty  component stub+a group proxy.
     * The stub in front of the group proxy is a component stub (instance of ComponentRepresentativeImpl),
     * that offers references to the functional interfaces defined in the type of the component.
     * @param componentType parameters of this component
     * @param controllerDesc TODO
     * @return a stub/proxy
     * @throws ClassNotFoundException
     * @throws java.lang.InstantiationException
     */
    public static ProActiveComponentRepresentative newComponentRepresentativeGroup(
            ComponentType componentType, ControllerDescription controllerDesc) throws ClassNotFoundException,
            InstantiationException {
        try {
            ProActiveComponentRepresentative result = null;

            // create the stub with the appropriate parameters
            Constructor<ProActiveComponentRepresentativeImpl> constructor = ProActiveComponentRepresentativeImpl.class
                    .getConstructor(new Class[] { ComponentType.class, String.class, String.class });
            result = (ProActiveComponentRepresentative) constructor
                    .newInstance(new Object[] { componentType, controllerDesc.getHierarchicalType(),
                            controllerDesc.getControllersConfigFileLocation() });

            // build the constructor call for the proxy object to create
            ConstructorCall reifiedCall = MOP.buildTargetObjectConstructorCall(
                    ProActiveComponentRepresentativeImpl.class, new Object[] { componentType,
                            controllerDesc.getHierarchicalType(),
                            controllerDesc.getControllersConfigFileLocation() });

            // Instanciates the proxy object
            ProxyForComponentGroup proxy = (ProxyForComponentGroup) MOP.createProxyObject(
                    ProxyForComponentGroup.class.getName(), MOP.EMPTY_OBJECT_ARRAY, reifiedCall);

            // connect the stub to the proxy
            result.setProxy(proxy);

            proxy.setClassName(Component.class.getName());
            proxy.setComponentType(componentType);
            proxy.setControllerDesc(controllerDesc);

            return result;
        } catch (Exception e) {
            InstantiationException ie = new InstantiationException(
                "cannot create group of component representatives : " + e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    /**
     * Creates an empty  non-functional component stub+a group proxy.
     * The stub in front of the group proxy is a non-functional component stub (instance of NFComponentRepresentativeImpl),
     * that offers references to the functional interfaces defined in the type of the component.
     * @param componentType parameters of this component
     * @param controllerDesc TODO
     * @return a stub/proxy
     * @throws ClassNotFoundException
     * @throws java.lang.InstantiationException
     */
    public static ProActiveComponentRepresentative newNFComponentRepresentativeGroup(
            ComponentType componentType, ControllerDescription controllerDesc) throws ClassNotFoundException,
            InstantiationException {
        try {
            ProActiveComponentRepresentative result = null;

            // create the stub with the appropriate parameters
            Constructor<ProActiveNFComponentRepresentativeImpl> constructor = ProActiveNFComponentRepresentativeImpl.class
                    .getConstructor(new Class[] { ComponentType.class, String.class, String.class });
            result = (ProActiveComponentRepresentative) constructor
                    .newInstance(new Object[] { componentType, controllerDesc.getHierarchicalType(),
                            controllerDesc.getControllersConfigFileLocation() });

            // build the constructor call for the proxy object to create
            ConstructorCall reifiedCall = MOP.buildTargetObjectConstructorCall(
                    ProActiveNFComponentRepresentativeImpl.class, new Object[] { componentType,
                            controllerDesc.getHierarchicalType(),
                            controllerDesc.getControllersConfigFileLocation() });

            // Instanciates the proxy object
            ProxyForComponentGroup proxy = (ProxyForComponentGroup) MOP.createProxyObject(
                    ProxyForComponentGroup.class.getName(), MOP.EMPTY_OBJECT_ARRAY, reifiedCall);

            // connect the stub to the proxy
            result.setProxy(proxy);

            proxy.setClassName(Component.class.getName());
            proxy.setComponentType(componentType);
            proxy.setControllerDesc(controllerDesc);

            return result;
        } catch (Exception e) {
            InstantiationException ie = new InstantiationException(
                "cannot create group of component representatives : " + e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }
}
