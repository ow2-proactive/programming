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
package org.objectweb.proactive.core.component.adl.bindings;

import static org.objectweb.proactive.core.component.adl.types.PATypeInterfaceUtil.isClient;
import static org.objectweb.proactive.core.component.adl.types.PATypeInterfaceUtil.isMandatory;
import static org.objectweb.proactive.core.component.adl.types.PATypeInterfaceUtil.isServer;
import static org.objectweb.proactive.core.component.adl.types.PATypeInterfaceUtil.isInternal;
import static org.objectweb.proactive.core.component.adl.types.PATypeInterfaceUtil.isCollective;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.bindings.Binding;
import org.objectweb.fractal.adl.bindings.BindingContainer;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.bindings.TypeBindingLoader;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.proactive.core.component.adl.types.PATypeInterface;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The {@link PATypeBindingLoader} extends the Fractal {@link TypeBindingLoader} to consider multicast/gathercast interfaces
 * when checking a binding, and when looking for interfaces. It also consider that the server interface maybe a WS interface.
 * 
 * The {@link PATypeBindingLoader} is also extended to explore the bindings defined inside {@link Controller}
 * nodes. These bindings are NF Bindings.
 * 
 * @author The ProActive Team
 */
public class PATypeBindingLoader extends TypeBindingLoader {

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    /** Checks if the binding is a multicast binding.
     * 
     * @param binding
     * @param itfMap
     * @return
     * @throws ADLException
     */
    protected boolean isMulticastBinding(final Binding binding,
            final Map<String, Map<String, Interface>> itfMap) throws ADLException {
        final String from = binding.getFrom();
        if (from == null) {
            throw new ADLException(BindingErrors.MISSING_FROM, binding);
        }
        int i = from.indexOf('.');
        if (i < 1 || i == from.length() - 1) {
            throw new ADLException(BindingErrors.INVALID_FROM_SYNTAX, binding, from);
        }
        final String fromCompName = from.substring(0, i);
        final String fromItfName = from.substring(i + 1);
        final Interface fromItf = getInterface(fromCompName, fromItfName, binding, itfMap);
        return GCMTypeFactory.MULTICAST_CARDINALITY.equals(((TypeInterface) fromItf).getCardinality());
    }

    /** 
     * Traverses the tree analyzing Bindings.
     * Bindings can be found inside BindingContainer nodes (definition, component, or controller)
     */
    @Override
    protected void checkNode(final Object node, final Map<Object, Object> context) throws ADLException {
        // only interested in BindingContainers
        if (node instanceof BindingContainer) {
            logger.debug("[PATypeBindingLoader] Analyzing bindings of " + node.toString() + ", " +
                ((BindingContainer) node).getBindings().length + ", " +
                (((Node) node).astGetDecoration("NF") != null ? "NF" : "F"));

            // Maps of F interface, membrane interfaces, and NF interfaces of subcomponents.
            // They are needed, because one F subcomponent and one NF component may have the same name.
            final Map<String, Map<String, Interface>> itfMap = new HashMap<String, Map<String, Interface>>();
            final Map<String, Map<String, Interface>> nfItfMap = new HashMap<String, Map<String, Interface>>();
            final Map<String, Map<String, Interface>> subNfItfMap = new HashMap<String, Map<String, Interface>>();

            // build the list of F interfaces of "this" component
            if (node instanceof InterfaceContainer) {
                final Map<String, Interface> containerItfs = new HashMap<String, Interface>();
                for (final Interface itf : ((InterfaceContainer) node).getInterfaces()) {
                    containerItfs.put(itf.getName(), itf);
                }
                itfMap.put("this", containerItfs);
            }
            // build the list of NF interfaces of "this" component
            if (node instanceof ControllerContainer) {
                Controller ctrl = ((ControllerContainer) node).getController();
                if (ctrl != null) {
                    if (ctrl instanceof InterfaceContainer) {
                        final Map<String, Interface> containerItfs = new HashMap<String, Interface>();
                        for (final Interface itf : ((InterfaceContainer) ctrl).getInterfaces()) {
                            containerItfs.put(itf.getName(), itf);
                        }
                        nfItfMap.put("this", containerItfs);
                    }
                }
            }

            // build the list of F interfaces of each F subcomponent
            if (node instanceof ComponentContainer) {
                for (final Component comp : ((ComponentContainer) node).getComponents()) {
                    if (comp instanceof InterfaceContainer) {
                        final Map<String, Interface> compItfs = new HashMap<String, Interface>();
                        for (final Interface itf : ((InterfaceContainer) comp).getInterfaces()) {
                            compItfs.put(itf.getName(), itf);
                        }
                        itfMap.put(comp.getName(), compItfs);
                    }
                }
            }
            // build the list of F interfaces of each NF component
            if (node instanceof ControllerContainer) {
                Controller ctrl = ((ControllerContainer) node).getController();
                if (ctrl != null) {
                    if (ctrl instanceof ComponentContainer) {
                        for (final Component comp : ((ComponentContainer) ctrl).getComponents()) {
                            if (comp instanceof InterfaceContainer) {
                                final Map<String, Interface> compItfs = new HashMap<String, Interface>();
                                for (final Interface itf : ((InterfaceContainer) comp).getInterfaces()) {
                                    compItfs.put(itf.getName(), itf);
                                }
                                nfItfMap.put(comp.getName(), compItfs);
                            }
                        }
                    }
                }
            }
            // build the list of NF interfaces of each F subcomponent
            if (node instanceof ComponentContainer) {
                for (final Component comp : ((ComponentContainer) node).getComponents()) {
                    if (comp instanceof ControllerContainer) {
                        Controller ctrl = ((ControllerContainer) comp).getController();
                        if (ctrl != null) {
                            if (ctrl instanceof InterfaceContainer) {
                                final Map<String, Interface> compItfs = new HashMap<String, Interface>();
                                for (final Interface itf : ((InterfaceContainer) ctrl).getInterfaces()) {
                                    compItfs.put(itf.getName(), itf);
                                }
                                subNfItfMap.put(comp.getName(), compItfs);
                            }
                        }
                    }
                }
            }

            // check each F binding (described in "this" node) using the list of collected interfaces
            for (final Binding binding : ((BindingContainer) node).getBindings()) {
                checkBinding(binding, itfMap, context);
            }
            // check each binding described in the membrane
            if (node instanceof ControllerContainer) {
                Controller ctrl = ((ControllerContainer) node).getController();
                if (ctrl != null) {
                    if (ctrl instanceof BindingContainer) {
                        for (final Binding binding : ((BindingContainer) ctrl).getBindings()) {
                            // TODO implement check for membrane bindings
                            checkMembraneBinding(binding, nfItfMap, subNfItfMap, context);
                        }
                    }
                }
            }

            // check for duplicated bindings, i.e. different bindings with the same "from" interface,
            // unless it is a multicast binding
            final Map<String, Binding> fromItfs = new HashMap<String, Binding>();
            for (final Binding binding : ((BindingContainer) node).getBindings()) {
                final Binding previousDefinition = fromItfs.put(binding.getFrom(), binding);
                // there is previous binding with same "from" interface, and it is not a multicast binding
                if ((previousDefinition != null) && !isMulticastBinding(binding, itfMap)) {
                    throw new ADLException(BindingErrors.DUPLICATED_BINDING, binding, binding.getFrom(),
                        previousDefinition);
                }
            }
        }

        // descend through a ControllerContainer and check all NF components
        if (node instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) node).getController();
            if (ctrl != null) {
                if (ctrl instanceof ComponentContainer) {
                    for (final Component comp : ((ComponentContainer) ctrl).getComponents()) {
                        checkNode(comp, context);
                    }
                }
            }
        }
        // descend through a ComponentContainer and check all F subcomponents
        if (node instanceof ComponentContainer) {
            for (final Component comp : ((ComponentContainer) node).getComponents()) {
                checkNode(comp, context);
            }
        }

    }

    /** 
     * Checks binding inside the membrane of "this" component.
     * There are two such kinds of bindings:<br/>
     * <ol>
     * <li>Bindings intra-membrane. From/to NF interfaces of "this" to/from F interface of NF components; or between F interfaces of NF components.
     *                              In both cases, it only requires to use the nfItfMap.</li>
     * <li>Bindings membrane - functional part. Between NF internal interfaces of "this" component, and NF interfaces of F subcomponents.
     *                                          It requires to look for interfaces in nfItfMap and subNfItfMap.</li>
     * </ol>
     * 
     * @param binding - The binding to check
     * @param nfItfMap - Map of F interfaces of NF components in the membrane of "this" component
     * @param subNfItfMap - Map of NF interfaces of F components in the functional part of "this" component
     * @param context
     */
    protected void checkMembraneBinding(Binding binding, Map<String, Map<String, Interface>> nfItfMap,
            Map<String, Map<String, Interface>> subNfItfMap, Map<Object, Object> context) throws ADLException {

        // obtain "from" and "to"
        final String from = binding.getFrom();
        final String to = binding.getTo();
        if (from == null) {
            throw new ADLException(BindingErrors.MISSING_FROM, binding);
        }
        if (to == null) {
            throw new ADLException(BindingErrors.MISSING_TO, binding);
        }

        // separate component and interface
        int i = from.indexOf('.');
        if (i < 1 || i == from.length() - 1) {
            throw new ADLException(BindingErrors.INVALID_FROM_SYNTAX, binding, from);
        }
        final String fromCompName = from.substring(0, i);
        final String fromItfName = from.substring(i + 1);

        i = to.indexOf('.');
        if (i < 1 || i == to.length() - 1) {
            throw new ADLException(BindingErrors.INVALID_TO_SYNTAX, binding, to);
        }
        final String toCompName = to.substring(0, i);
        final String toItfName = to.substring(i + 1);

        // ignore the special "component" interface
        if (ignoreBinding(binding, fromCompName, fromItfName, toCompName, toItfName)) {
            return;
        }

        // Obtain the interfaces from the maps.
        // Case (1): bindings intra-membrane
        // Case (2): bindings membrane-functionalPart

        Interface fromItf = null;
        Interface toItf = null;

        // remember, NF interfaces must end with "-controller" (but this is not being enforced here, for the moment)

        // Retrieve the interface objects from the interface maps

        // if the 'from' component is "this", then the interface must be in the nfItfMap
        if ("this".equals(fromCompName)) {
            fromItf = getInterface(fromCompName, fromItfName, binding, nfItfMap);
        } else {
            // else, it may be in the nfItfMap, or in the subNfItfMap
            try {
                fromItf = getInterface(fromCompName, fromItfName, binding, nfItfMap);
            } catch (ADLException ae) {
                // if it's not there, then it must be in the subNfItfMap
                fromItf = getInterface(fromCompName, fromItfName, binding, subNfItfMap);
            }
        }

        // if the 'to' component is "this", then the interface must be in the nfItfMap
        if ("this".equals(toCompName)) {
            toItf = getInterface(toCompName, toItfName, binding, nfItfMap);
        } else {
            // else, it may be in the nfItfMap, or in the subNfItfMap
            try {
                toItf = getInterface(toCompName, toItfName, binding, nfItfMap);
            } catch (ADLException ae) {
                // if it's not there, then it must be in the subNfItfMap
                toItf = getInterface(toCompName, toItfName, binding, subNfItfMap);
            }
        }

        // and check the binding
        checkBinding(binding, fromItf, fromCompName, fromItfName, toItf, toCompName, toItfName, context);

    }

    /** 
     * Checks a binding.
     * The 'super' method verifies the server/client quality of the interfaces, and if the client interface is superclass 
     * or the same class as server (i.e., serverClass is the the same or extends clientClass)
     * 
     * If that fails, this method verifies the multicast interfaces (which should have thrown an exception in the 'super'
     * method), and checks that they have the same number of methods. 
     * 
     * NOTE: It is not a complete multicast compatibility test. However, a complete checking is done inside the GCM API methods
     * (see {@link PAMulticastController})
     * 
     */
    @Override
    protected void checkBinding(final Binding binding, final Map<String, Map<String, Interface>> itfMap,
            final Map<Object, Object> context) throws ADLException {
        try {
            super.checkBinding(binding, itfMap, context);
        } catch (ADLException adle) {
            // checks if trying to bind to a web service
            if (!binding.getTo().contains("://")) {
                throw adle;
            }
        }
    }

    @Override
    protected void checkBinding(final Binding binding, final Interface fromItf, final String fromCompName,
            final String fromItfName, final Interface toItf, final String toCompName, final String toItfName,
            final Map<Object, Object> context) throws ADLException {
        try {
            logger.debug("[PATypeBindingLoader] Checking binding " + fromItf + "(" +
                (fromItf.astGetDecoration("NF") != null ? "NF" : "F") + ")--> " + toItf + "(" +
                (toItf.astGetDecoration("NF") != null ? "NF" : "F") + ")");
            // the 'super' method
            if (fromItf instanceof PATypeInterface && toItf instanceof PATypeInterface) {
                final PATypeInterface cItf = (PATypeInterface) fromItf;
                final PATypeInterface sItf = (PATypeInterface) toItf;
                if (fromCompName.equals("this")) {
                    // 'from' interfaces of 'this' must be server, or internal-client 
                    if (!isServer(cItf) && !isInternal(cItf)) {
                        throw new ADLException(PABindingErrors.INVALID_FROM_INTERNAL, binding, fromItfName,
                            new NodeErrorLocator(cItf));
                    }
                } else {
                    // 'from' interfaces of inner components must be client
                    if (!isClient(cItf)) {
                        throw new ADLException(BindingErrors.INVALID_FROM_NOT_A_CLIENT, binding, binding
                                .getFrom(), new NodeErrorLocator(cItf));
                    }
                }
                if (toCompName.equals("this")) {
                    // 'to' interfaces of 'this' must client, or internal-server
                    if (!isClient(sItf) && !isInternal(sItf)) {
                        throw new ADLException(PABindingErrors.INVALID_TO_INTERNAL, binding, toItfName,
                            new NodeErrorLocator(cItf));
                    }
                } else {
                    // 'to' interfaces of inner component must be server
                    if (!isServer(sItf)) {
                        throw new ADLException(BindingErrors.INVALID_TO_NOT_A_SERVER, binding, binding
                                .getTo(), new NodeErrorLocator(sItf));
                    }
                }

                // disallow [mandatory --> optional] bindings
                if (isMandatory(cItf) && !isMandatory(sItf)) {
                    throw new ADLException(BindingErrors.INVALID_MANDATORY_TO_OPTIONAL, binding, binding
                            .getFrom(), binding.getTo());
                }

                // check that class are loadable and assignable
                if (interfaceCodeLoaderItf != null) {
                    Object cClass = null;
                    Object sClass = null;
                    cClass = interfaceCodeLoaderItf.loadInterface(cItf.getSignature(), context);
                    sClass = interfaceCodeLoaderItf.loadInterface(sItf.getSignature(), context);

                    if ((cClass instanceof Class) && (sClass instanceof Class) &&
                        !((Class<?>) cClass).isAssignableFrom((Class<?>) sClass)) {
                        // cClass cannot be assigned sClass ... one of them maybe a Collective (Multicast/Gathercast) interface
                        if (isCollective(cItf) || isCollective(sItf)) {
                            // at least the number of methods must coincide. Although, a complete compatibility check is performed later.
                            Method[] clientSideItfMethods = ((Class<?>) cClass).getMethods();
                            Method[] serverSideItfMethods = ((Class<?>) sClass).getMethods();
                            if (clientSideItfMethods.length != serverSideItfMethods.length) {
                                throw new ADLException(PABindingErrors.INVALID_COLLECTIVE_SIGNATURE, fromItf,
                                    toItf);
                            }
                        }
                        // not multicast nor gathercast ... nothing to do, throw the exception
                        else {
                            throw new ADLException(PABindingErrors.INVALID_SIGNATURE, binding,
                                new NodeErrorLocator(fromItf), new NodeErrorLocator(toItf));
                        }
                    }
                }
            }
        } catch (ADLException e) {
            // TODO remove this catch instruction and integrate it with the rest of the method
            // check if signatures are incompatible
            TypeInterface cItf = (TypeInterface) fromItf;
            TypeInterface sItf = (TypeInterface) toItf;

            Class<?> clientSideItfClass = (Class<?>) interfaceCodeLoaderItf.loadInterface(
                    cItf.getSignature(), context);
            Class<?> serverSideItfClass = (Class<?>) interfaceCodeLoaderItf.loadInterface(
                    sItf.getSignature(), context);
            if (!clientSideItfClass.isAssignableFrom(serverSideItfClass)) {
                // check if multicast interface
                if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(cItf.getCardinality())) {
                    Method[] clientSideItfMethods = clientSideItfClass.getMethods();
                    Method[] serverSideItfMethods = serverSideItfClass.getMethods();

                    if (clientSideItfMethods.length != serverSideItfMethods.length) {
                        throw new ADLException(PABindingErrors.INVALID_COLLECTIVE_SIGNATURE, fromItf, toItf);
                    }
                    // ok, at least in number of methods
                    return;
                }
                // client class is not multicast, then re-throw e
                throw e;
            }
            // the catched exception is still valid
            throw e;
        }
    }

    /**
     * Finds an Interface node. 
     * If both checks fail (it is not a singleton interface, nor it is a collection interface),
     * then it checks if it is a webservice interface. In that case null is returned.
     */
    @Override
    protected Interface getInterface(final String compName, final String itfName, final Node sourceNode,
            final Map<String, Map<String, Interface>> itfMap) throws ADLException {
        try {
            return super.getInterface(compName, itfName, sourceNode, itfMap);
        } catch (final ADLException ae) {
            // The interface can't be found. Either it is a web service URL or it is really an error.
            if (compName.contains("://")) {
                return null;
            } else {
                throw ae;
            }
        }
    }

    /**
     * Overrides the corresponding method from {@link BindingLoader} to NOT ignore the interfaces
     * that end by "-controller"
     */
    @Override
    protected boolean ignoreBinding(final Binding binding, final String fromCompName,
            final String fromItfName, final String toCompName, final String toItfName) {
        return fromItfName.equals("component") || toItfName.equals("component");
    }

}
