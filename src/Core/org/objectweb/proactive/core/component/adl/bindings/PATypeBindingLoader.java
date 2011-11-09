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
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
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
	
	public static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);
	
	/** Checks if the binding is a multicast binding.
	 * 
	 * @param binding
	 * @param itfMap
	 * @return
	 * @throws ADLException
	 */
    protected boolean isMulticastBinding(final Binding binding, final Map<String, Map<String, Interface>> itfMap)
            throws ADLException {
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
        	logger.debug("[PATypeBindingLoader] Analyzing bindings of "+ node.toString() + ", "+ ((BindingContainer)node).getBindings().length + ", "+ (((Node)node).astGetDecoration("NF")!=null?"NF":"F"));
            final Map<String, Map<String, Interface>> itfMap = new HashMap<String, Map<String, Interface>>();
            // build the list of interfaces of "this" component
            if (node instanceof InterfaceContainer) {
                final Map<String, Interface> containerItfs = new HashMap<String, Interface>();
                for (final Interface itf : ((InterfaceContainer) node).getInterfaces()) {
                    containerItfs.put(itf.getName(), itf);
                }
                itfMap.put("this", containerItfs);
            }
            // build the list of interfaces of each subcomponent
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
            // check each binding described in "this" node using the list of collected interfaces
            for (final Binding binding : ((BindingContainer) node).getBindings()) {
                checkBinding(binding, itfMap, context);
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
        // descend through a ComponentContainer
        if (node instanceof ComponentContainer) {
            for (final Component comp : ((ComponentContainer) node).getComponents()) {
                checkNode(comp, context);
            }
        }
    	// descend also through a ControllerContainer, because a Controller can also contain (NF) components
    	if (node instanceof ControllerContainer) {
    		Controller ctrl = ((ControllerContainer) node).getController();
    		if(ctrl != null) {
    			checkNode(ctrl, context);
    		}
    	}
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
    protected void checkBinding(final Binding binding, final Interface fromItf, final String fromCompName,
            final String fromItfName, final Interface toItf, final String toCompName, final String toItfName,
            final Map<Object, Object> context) throws ADLException {
        try {
        	logger.debug("[PATypeBindingLoader] Checking binding "+ fromItf + "("+ (fromItf.astGetDecoration("NF")!=null?"NF":"F") +")--> "+ toItf + "("+(toItf.astGetDecoration("NF")!=null?"NF":"F")+")");
            super.checkBinding(binding, fromItf, fromCompName, fromItfName, toItf, toCompName, toItfName,
                    context);
        } catch (ADLException e) {
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
                        throw new ADLException(PABindingErrors.INVALID_MULTICAST_SIGNATURE, fromItf, toItf);
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
}
