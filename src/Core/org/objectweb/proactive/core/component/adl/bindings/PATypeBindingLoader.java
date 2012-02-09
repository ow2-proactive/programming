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

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.bindings.Binding;
import org.objectweb.fractal.adl.bindings.BindingContainer;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.bindings.TypeBindingLoader;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;


/**
 * @author The ProActive Team
 */
public class PATypeBindingLoader extends TypeBindingLoader {
    private boolean isMulticastBinding(final Binding binding, final Map<String, Map<String, Interface>> itfMap)
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

    @Override
    protected void checkNode(final Object node, final Map<Object, Object> context) throws ADLException {
        if (node instanceof BindingContainer) {
            final Map<String, Map<String, Interface>> itfMap = new HashMap<String, Map<String, Interface>>();
            if (node instanceof InterfaceContainer) {
                final Map<String, Interface> containerItfs = new HashMap<String, Interface>();
                for (final Interface itf : ((InterfaceContainer) node).getInterfaces()) {
                    containerItfs.put(itf.getName(), itf);
                }
                itfMap.put("this", containerItfs);
            }
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
            for (final Binding binding : ((BindingContainer) node).getBindings()) {
                checkBinding(binding, itfMap, context);
            }
            final Map<String, Binding> fromItfs = new HashMap<String, Binding>();
            for (final Binding binding : ((BindingContainer) node).getBindings()) {
                final Binding previousDefinition = fromItfs.put(binding.getFrom(), binding);
                if ((previousDefinition != null) && !isMulticastBinding(binding, itfMap)) {
                    throw new ADLException(BindingErrors.DUPLICATED_BINDING, binding, binding.getFrom(),
                        previousDefinition);
                }
            }
        }
        if (node instanceof ComponentContainer) {
            for (final Component comp : ((ComponentContainer) node).getComponents()) {
                checkNode(comp, context);
            }
        }
    }

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
            super.checkBinding(binding, fromItf, fromCompName, fromItfName, toItf, toCompName, toItfName,
                    context);
        } catch (ADLException adle) {
            // checks if signatures are incompatible
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
                }
            }
        }
    }

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
