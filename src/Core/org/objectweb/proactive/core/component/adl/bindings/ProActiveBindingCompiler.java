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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.adl.bindings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.component.api.InstanceProviderTask;
import org.objectweb.deployment.scheduling.core.api.Task;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.bindings.Binding;
import org.objectweb.fractal.adl.bindings.BindingBuilder;
import org.objectweb.fractal.adl.bindings.BindingCompiler;
import org.objectweb.fractal.adl.bindings.BindingContainer;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.ComponentPair;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;


public class ProActiveBindingCompiler extends BindingCompiler {
    private static Map<String, Integer> multicastItfTaskIndexer = new HashMap<String, Integer>();

    // --------------------------------------------------------------------------
    // Implementation of the Compiler interface
    // --------------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public void compile(final List path, final ComponentContainer container, final TaskMap tasks,
            final Map context) throws ADLException {
        Map subComponents = new HashMap();
        subComponents.put("this", container);
        Component[] comps = container.getComponents();
        for (int i = 0; i < comps.length; i++) {
            subComponents.put(comps[i].getName(), comps[i]);
        }

        if (container instanceof BindingContainer) {
            Binding[] bindings = ((BindingContainer) container).getBindings();
            for (int i = 0; i < bindings.length; i++) {
                Binding binding = bindings[i];

                int type = BindingBuilder.NORMAL_BINDING;
                if (binding.getFrom().startsWith("this.")) {
                    type = BindingBuilder.EXPORT_BINDING;
                }
                if (binding.getTo().startsWith("this.")) {
                    type = BindingBuilder.IMPORT_BINDING;
                }
                if (binding.getTo().contains("://")) {
                    type = ProActiveBindingBuilder.WEBSERVICE_BINDING;
                }

                String value = binding.getFrom();
                int index = value.indexOf('.');
                Object clientComp = subComponents.get(value.substring(0, index));
                String clientItf = value.substring(index + 1);
                InstanceProviderTask createClientTask = (InstanceProviderTask) tasks.getTask("create",
                        clientComp);

                value = binding.getTo();
                Object serverComp = null;
                String serverItf = null;
                InstanceProviderTask createServerTask = null;
                if (type != ProActiveBindingBuilder.WEBSERVICE_BINDING) {
                    index = value.indexOf('.');
                    serverComp = subComponents.get(value.substring(0, index));
                    serverItf = value.substring(index + 1);
                    createServerTask = (InstanceProviderTask) tasks.getTask("create", serverComp);
                } else {
                    serverItf = value;
                }

                try {
                    // the task may already exist, in case of a shared component
                    // but multicast interfaces are handled specifically
                    if ((BindingBuilder.EXPORT_BINDING == type) && (container instanceof BindingContainer) &&
                        (container instanceof InterfaceContainer)) {
                        Interface[] itfs = ((InterfaceContainer) container).getInterfaces();
                        for (int j = 0; j < itfs.length; j++) {
                            TypeInterface itf = (TypeInterface) itfs[j];
                            if (clientItf.equals(itf.getName())) {
                                if (ProActiveTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
                                    throw new NoSuchElementException(clientItf);
                                }
                            }
                        }
                    } else if ((clientComp instanceof BindingContainer) &&
                        (clientComp instanceof InterfaceContainer)) {
                        Interface[] itfs = ((InterfaceContainer) clientComp).getInterfaces();
                        for (int j = 0; j < itfs.length; j++) {
                            TypeInterface itf = (TypeInterface) itfs[j];
                            if (clientItf.equals(itf.getName())) {
                                if (ProActiveTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
                                    throw new NoSuchElementException(clientItf);
                                }
                            }
                        }
                    }

                    tasks.getTask("bind" + clientItf, clientComp);
                } catch (NoSuchElementException e) {
                    ProActiveBindTask bindTask = new ProActiveBindTask(builder, type, clientItf, serverItf);
                    bindTask.setInstanceProviderTask(createClientTask);
                    bindTask.setServerInstanceProviderTask(createServerTask);

                    // add an index to the task in case of a multicast binding
                    if ((BindingBuilder.EXPORT_BINDING == type) && (container instanceof BindingContainer) &&
                        (container instanceof InterfaceContainer)) {
                        // export binding : use current container
                        InterfaceContainer itfContainer = ((InterfaceContainer) container);
                        clientItf = setMulticastIndex(clientComp, clientItf, itfContainer);
                    } else if ((clientComp instanceof BindingContainer) &&
                        (clientComp instanceof InterfaceContainer)) {
                        // normal binding : use client component as container
                        InterfaceContainer itfContainer = ((InterfaceContainer) clientComp);
                        clientItf = setMulticastIndex(clientComp, clientItf, itfContainer);
                    }

                    tasks.addTask("bind" + clientItf, clientComp, bindTask);

                    if (clientComp != container) {
                        Task addTask = tasks.getTask("add", new ComponentPair(container,
                            (Component) clientComp));
                        bindTask.addPreviousTask(addTask);
                    }
                    if ((type != ProActiveBindingBuilder.WEBSERVICE_BINDING) && (serverComp != container)) {
                        Task addTask = tasks.getTask("add", new ComponentPair(container,
                            (Component) serverComp));
                        bindTask.addPreviousTask(addTask);
                    }

                    Task startTask = tasks.getTask("start", clientComp);
                    startTask.addPreviousTask(bindTask);
                }
            }
        }
    }

    /**
     * @param clientComp
     * @param clientItf
     * @param itfContainer
     * @return
     */
    private String setMulticastIndex(Object clientComp, String clientItf, InterfaceContainer itfContainer) {
        Interface[] itfs = itfContainer.getInterfaces();
        for (int j = 0; j < itfs.length; j++) {
            TypeInterface itf = (TypeInterface) itfs[j];
            if (clientItf.equals(itf.getName())) {
                if (ProActiveTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
                    // ok, this is a multicast interface => multiple bindings allowed from this interface
                    // ==> need to create several tasks
                    int multicastIndex = 1;
                    if (multicastItfTaskIndexer.containsKey(clientComp + clientItf)) {
                        // increment and update
                        multicastIndex += multicastItfTaskIndexer.get(clientComp + clientItf);
                        multicastItfTaskIndexer.put(clientComp + clientItf, multicastIndex);
                    } else {
                        // initialize for this interface
                        multicastItfTaskIndexer.put(clientComp + clientItf, multicastIndex);
                    }
                    clientItf = clientItf + "-" + multicastIndex;
                }
            }
        }
        return clientItf;
    }

    static class ProActiveBindTask extends BindTask {
        private BindingBuilder proActiveBuilder;

        private int type;

        private String clientItf;

        private String serverItf;

        public ProActiveBindTask(final BindingBuilder builder, final int type, final String clientItf,
                final String serverItf) {
            super(builder, type, clientItf, serverItf);
            this.proActiveBuilder = new ProActiveBindingBuilder();
            this.type = type;
            this.clientItf = clientItf;
            this.serverItf = serverItf;
        }

        public void execute(final Object context) throws Exception {
            if (type != ProActiveBindingBuilder.WEBSERVICE_BINDING) {
                super.execute(context);
            } else {
                Object client = getInstanceProviderTask().getInstance();
                proActiveBuilder.bindComponent(type, client, clientItf, null, serverItf, context);
            }
        }
    }
}
