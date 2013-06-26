/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package org.objectweb.proactive.core.component;

import java.util.ArrayList;
import java.util.Map;

import org.etsi.uri.gcm.api.type.GCMInterfaceType;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.Factory;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.control.PAMigrationController;
import org.objectweb.proactive.core.component.control.PAMulticastController;
import org.objectweb.proactive.core.component.control.PASuperController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAComponentType;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;


/**
 * Utility methods for components.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class Utils {
    /**
     * Returns a bootstrap component to create other components.
     *
     * @return Bootstrap component to create other components.
     * @throws InstantiationException If the bootstrap component cannot be created.
     */
    public static Component getBootstrapComponent() throws InstantiationException {
        Component bootstrapComponent;
        try {
            bootstrapComponent = GCM.getBootstrapComponent();
        } catch (InstantiationException ie) {
            if (System.getProperty("gcm.provider") == null) {
                try {
                    bootstrapComponent = Fractal.getBootstrapComponent();
                } catch (InstantiationException ie2) {
                    if (System.getProperty("fractal.provider") == null) {
                        throw new InstantiationException(
                            "Neither the gcm.provider or the fractal.provider system properties are defined");
                    } else {
                        throw ie2;
                    }
                }
            } else {
                throw ie;
            }
        }
        return bootstrapComponent;
    }

    /**
     * Returns a bootstrap component to create other components. This method creates an instance of the class
     * whose name is associated to the "gcm.provider" or the "fractal.provider" key, which must implement the
     * {@link Factory} or {@link GenericFactory} interface, and returns the component instantiated by this
     * factory.
     *
     * @param hints {@link Map} which must associate a value to the "gcm.provider" or the "fractal.provider"
     * key, and which may associate a {@link ClassLoader} to the "classloader" key. This class loader will
     * be used to load the bootstrap component.
     * @return Bootstrap component to create other components.
     * @throws InstantiationException If the bootstrap component cannot be created.
     */
    public static Component getBootstrapComponent(final Map<?, ?> hints) throws InstantiationException {
        Component bootstrapComponent;
        try {
            bootstrapComponent = GCM.getBootstrapComponent(hints);
        } catch (InstantiationException ie) {
            if (System.getProperty("gcm.provider") == null) {
                try {
                    bootstrapComponent = Fractal.getBootstrapComponent(hints);
                } catch (InstantiationException ie2) {
                    if (System.getProperty("fractal.provider") == null) {
                        throw new InstantiationException(
                            "Neither the gcm.provider or the fractal.provider system properties are defined");
                    } else {
                        throw ie2;
                    }
                }
            } else {
                throw ie;
            }
        }
        return bootstrapComponent;
    }

    /**
     * Returns the {@link PABindingController} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PABindingController} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PABindingController getPABindingController(final Component component)
            throws NoSuchInterfaceException {
        return (PABindingController) component.getFcInterface(Constants.BINDING_CONTROLLER);
    }

    /**
     * Returns the {@link PAContentController} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PAContentController} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PAContentController getPAContentController(final Component component)
            throws NoSuchInterfaceException {
        return (PAContentController) component.getFcInterface(Constants.CONTENT_CONTROLLER);
    }

    /**
     * Returns the {@link PASuperController} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PASuperController} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PASuperController getPASuperController(final Component component)
            throws NoSuchInterfaceException {
        return (PASuperController) component.getFcInterface(Constants.SUPER_CONTROLLER);
    }

    /**
     * Returns the {@link PAGCMLifeCycleController} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PAGCMLifeCycleController} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PAGCMLifeCycleController getPAGCMLifeCycleController(final Component component)
            throws NoSuchInterfaceException {
        return (PAGCMLifeCycleController) component.getFcInterface(Constants.LIFECYCLE_CONTROLLER);
    }

    /**
     * Returns the {@link PAMulticastController} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PAMulticastController} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PAMulticastController getPAMulticastController(final Component component)
            throws NoSuchInterfaceException {
        return (PAMulticastController) component.getFcInterface(Constants.MULTICAST_CONTROLLER);
    }

    /**
     * Returns the {@link PAMigrationController} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PAMigrationController} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PAMigrationController getPAMigrationController(final Component component)
            throws NoSuchInterfaceException {
        return (PAMigrationController) component.getFcInterface(Constants.MIGRATION_CONTROLLER);
    }

    /**
     * Returns the {@link PAMembraneController} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PAMembraneController} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PAMembraneController getPAMembraneController(final Component component)
            throws NoSuchInterfaceException {
        return (PAMembraneController) component.getFcInterface(Constants.MEMBRANE_CONTROLLER);
    }

    /**
     * Returns the {@link PAGenericFactory} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PAGenericFactory} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PAGenericFactory getPAGenericFactory(final Component component)
            throws NoSuchInterfaceException {
        return (PAGenericFactory) component.getFcInterface(Constants.GENERIC_FACTORY);
    }

    /**
     * Returns the {@link PAGCMTypeFactory} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PAGCMTypeFactory} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PAGCMTypeFactory getPAGCMTypeFactory(final Component component)
            throws NoSuchInterfaceException {
        return (PAGCMTypeFactory) component.getFcInterface(Constants.TYPE_FACTORY);
    }

    /**
     * Checks whether a component interface name match a controller interface. According to the
     * Fractal specification a controller interface name is either "component" or ends with
     * "-controller".
     *
     * @param itfName Component interface name.
     * @return True if it is a controller interface name.
     */
    public static boolean isControllerItfName(String itfName) {
        // according to Fractal spec v2.0 , section 4.1
        return ((itfName != null) && (itfName.endsWith("-controller") || itfName.equals(Constants.COMPONENT)));
    }

    /**
     * Checks whether a component interface is a client interface.
     *
     * @param itf Component interface.
     * @return True if the given interface is a client interface.
     */
    public static boolean isGCMClientItf(Interface itf) {
        return ((GCMInterfaceType) itf.getFcItfType()).isFcClientItf();
    }

    /**
     * Returns the cardinality of the given component interface name.
     *
     * @param itfName Component interface name.
     * @param owner Reference on the component owner of the given component interface name.
     * @return Cardinality of the given interface name.
     * @throws NoSuchInterfaceException If the component has no such component interface name.
     */
    public static String getGCMCardinality(String itfName, Component owner) throws NoSuchInterfaceException {
        InterfaceType[] itfTypes = null;
        ComponentType componentType = (ComponentType) owner.getFcType();
        if (componentType instanceof PAComponentType) {
            itfTypes = ((PAComponentType) componentType).getAllFcInterfaceTypes();
        } else {
            itfTypes = componentType.getFcInterfaceTypes();
        }

        for (InterfaceType itfType : itfTypes) {
            if (itfType.getFcItfName().equals(itfName)) {
                return ((GCMInterfaceType) itfType).getGCMCardinality();
            } else if (itfType.isFcCollectionItf() && itfName.startsWith(itfType.getFcItfName())) {
                return GCMTypeFactory.COLLECTION_CARDINALITY;
            }
        }
        throw new NoSuchInterfaceException(itfName);
    }

    /**
     * Checks whether a component interface has a singleton cardinality.
     *
     * @param itf Component interface.
     * @return True if the given interface has a singleton cardinality.
     */
    public static boolean isGCMSingletonItf(Interface itf) {
        return ((GCMInterfaceType) itf.getFcItfType()).isGCMSingletonItf();
    }

    /**
     * Checks whether a component interface has a collection cardinality.
     *
     * @param itf Component interface.
     * @return True if the given interface has a collection cardinality.
     */
    public static boolean isGCMCollectionItf(Interface itf) {
        return ((GCMInterfaceType) itf.getFcItfType()).isGCMCollectionItf();
    }

    /**
     * Checks whether a component interface has a multicast cardinality.
     *
     * @param itf Component interface.
     * @return True if the given interface has a multicast cardinality.
     */
    public static boolean isGCMMulticastItf(Interface itf) {
        return ((GCMInterfaceType) itf.getFcItfType()).isGCMMulticastItf();
    }

    /**
     * Checks whether a component interface has a gathercast cardinality.
     *
     * @param itf Component interface.
     * @return True if the given interface has a gathercast cardinality.
     */
    public static boolean isGCMGathercastItf(Interface itf) {
        return ((GCMInterfaceType) itf.getFcItfType()).isGCMGathercastItf();
    }

    /**
     * Checks whether a component interface has a collective cardinality.
     *
     * @param itf Component interface.
     * @return True if the given interface has a collective cardinality.
     */
    public static boolean isGCMCollectiveItf(Interface itf) {
        return ((PAGCMInterfaceType) itf.getFcItfType()).isGCMCollectiveItf();
    }

    /**
     * Checks whether a component interface is a client interface.
     *
     * @param itfName Component interface name.
     * @param owner Reference on the component owner of the given component interface name.
     * @return True if the given interface is a client interface.
     * @throws NoSuchInterfaceException If the component has no such component interface name.
     */
    public static boolean isGCMClientItf(String itfName, Component owner) throws NoSuchInterfaceException {
        return isGCMClientItf((Interface) owner.getFcInterface(itfName));
    }

    /**
     * Checks whether a component interface name has a singleton cardinality.
     *
     * @param itfName Component interface name.
     * @param owner Reference on the component owner of the given component interface name.
     * @return True if the given interface name has a singleton cardinality.
     * @throws NoSuchInterfaceException If the component has no such component interface name.
     */
    public static boolean isGCMSingletonItf(String itfName, Component owner) throws NoSuchInterfaceException {
        return GCMTypeFactory.SINGLETON_CARDINALITY.equals(getGCMCardinality(itfName, owner));
    }

    /**
     * Checks whether a component interface name has a collection cardinality.
     *
     * @param itfName Component interface name.
     * @param owner Reference on the component owner of the given component interface name.
     * @return True if the given interface name has a collection cardinality.
     * @throws NoSuchInterfaceException If the component has no such component interface name.
     */
    public static boolean isGCMCollectionItf(String itfName, Component owner) throws NoSuchInterfaceException {
        return GCMTypeFactory.COLLECTION_CARDINALITY.equals(getGCMCardinality(itfName, owner));
    }

    /**
     * Checks whether a component interface name has a multicast cardinality.
     *
     * @param itfName Component interface name.
     * @param owner Reference on the component owner of the given component interface name.
     * @return True if the given interface name has a multicast cardinality.
     * @throws NoSuchInterfaceException If the component has no such component interface name.
     */
    public static boolean isGCMMulticastItf(String itfName, Component owner) throws NoSuchInterfaceException {
        return GCMTypeFactory.MULTICAST_CARDINALITY.equals(getGCMCardinality(itfName, owner));
    }

    /**
     * Checks whether a component interface name has a gathercast cardinality.
     *
     * @param itfName Component interface name.
     * @param owner Reference on the component owner of the given component interface name.
     * @return True if the given interface name has a gathercast cardinality.
     * @throws NoSuchInterfaceException If the component has no such component interface name.
     */
    public static boolean isGCMGathercastItf(String itfName, Component owner) throws NoSuchInterfaceException {
        return GCMTypeFactory.GATHERCAST_CARDINALITY.equals(getGCMCardinality(itfName, owner));
    }

    /**
     * Checks whether a component interface name has a collective (multicast or gathercast) cardinality.
     *
     * @param itfName Component interface name.
     * @param owner Reference on the component owner of the given component interface name.
     * @return True if the given interface name has a collective cardinality.
     */
    public static boolean isGCMCollectiveItf(String itfName, Component owner) {
        try {
            return isGCMMulticastItf(itfName, owner) || isGCMGathercastItf(itfName, owner);
        } catch (NoSuchInterfaceException e) {
            return false;
        }
    }

    /**
     * Helper method for extracting the types of client interfaces from the type of a component.
     *
     * @param componentType Component type.
     * @return Types of client interfaces.
     */
    public static InterfaceType[] getClientItfTypes(ComponentType componentType) {
        ArrayList<InterfaceType> clientItfs = new ArrayList<InterfaceType>();
        InterfaceType[] itfTypes = componentType.getFcInterfaceTypes();
        for (int i = 0; i < itfTypes.length; i++) {
            if (itfTypes[i].isFcClientItf()) {
                clientItfs.add(itfTypes[i]);
            }
        }
        return clientItfs.toArray(new InterfaceType[clientItfs.size()]);
    }
}
