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
package org.objectweb.proactive.core.component.adl.bindings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.adl.ADLException;
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
import org.objectweb.fractal.task.core.Task;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.fractal.task.core.TaskMap.TaskHole;
import org.objectweb.fractal.task.deployment.api.InstanceProviderTask;
import org.objectweb.fractal.task.deployment.lib.AbstractRequireInstanceProviderTask;


public class PABindingCompiler extends BindingCompiler {
    private static Map<String, Integer> multicastItfTaskIndexer = new HashMap<String, Integer>();

    // --------------------------------------------------------------------------
    // Implementation of the Compiler interface
    // --------------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public void compile(final List path, final ComponentContainer container,
            final org.objectweb.fractal.task.core.TaskMap tasks, final Map context) throws ADLException {
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
                    type = PABindingBuilder.WEBSERVICE_BINDING;
                }

                String value = binding.getFrom();
                int index = value.indexOf('.');
                Object clientComp = subComponents.get(value.substring(0, index));
                String clientItf = value.substring(index + 1);
                TaskHole createClientTask = tasks.getTaskHole("create", clientComp);

                value = binding.getTo();
                Object serverComp = null;
                String serverItf = null;
                TaskHole createServerTask = null;
                if (type != PABindingBuilder.WEBSERVICE_BINDING) {
                    index = value.indexOf('.');
                    serverComp = subComponents.get(value.substring(0, index));
                    serverItf = value.substring(index + 1);
                    createServerTask = tasks.getTaskHole("create", serverComp);
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
                                if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
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
                                if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
                                    throw new NoSuchElementException(clientItf);
                                }
                            }
                        }
                    }

                    tasks.getTask("bind" + clientItf, clientComp);
                } catch (NoSuchElementException e) {
                    PABindTask bindTask = new PABindTask(builder, type, clientItf, serverItf);
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

                    TaskHole bindTaskHole = tasks.addTask("bind" + clientItf, clientComp, bindTask);

                    if (clientComp != container) {
                        TaskHole addTask = tasks.getTaskHole("add", new ComponentPair(container,
                            (Component) clientComp));
                        bindTask.addDependency(addTask, Task.PREVIOUS_TASK_ROLE, context);
                        //bindTask.addPreviousTask(addTask);
                    }
                    if ((type != PABindingBuilder.WEBSERVICE_BINDING) && (serverComp != container)) {
                        TaskHole addTask = tasks.getTaskHole("add", new ComponentPair(container,
                            (Component) serverComp));
                        bindTask.addDependency(addTask, Task.PREVIOUS_TASK_ROLE, context);
                        //bindTask.addPreviousTask(addTask);
                    }

                    TaskHole startTask = tasks.getTaskHole("start", clientComp);
                    startTask.addDependency(bindTaskHole, Task.PREVIOUS_TASK_ROLE, context);
                    //startTask.addPreviousTask(bindTask);
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
                if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
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

    static class PABindTask extends AbstractRequireInstanceProviderTask {
        private TaskMap.TaskHole serverInstanceProviderTask;

        private BindingBuilder builder;

        private int type;

        private String clientItf;

        private String serverItf;

        public PABindTask(final BindingBuilder builder, final int type, final String clientItf,
                final String serverItf) {
            this.builder = builder;
            this.type = type;
            this.clientItf = clientItf;
            this.serverItf = serverItf;
        }

        public InstanceProviderTask getServerInstanceProviderTask() {
            return (serverInstanceProviderTask) == null ? null
                    : (InstanceProviderTask) serverInstanceProviderTask.getTask();
        }

        public void setServerInstanceProviderTask(final TaskMap.TaskHole task) {
            if (serverInstanceProviderTask != null) {
                removePreviousTask(serverInstanceProviderTask);
            }
            serverInstanceProviderTask = task;
            if (serverInstanceProviderTask != null) {
                addPreviousTask(serverInstanceProviderTask);
            }
        }

        public void execute(final Map<Object, Object> context) throws Exception {
            Object client = null;
            Object server = null;
            if (type != PABindingBuilder.WEBSERVICE_BINDING) {
                client = getInstanceProviderTask().getInstance();
                server = getServerInstanceProviderTask().getInstance();
            } else {
                client = getInstanceProviderTask().getInstance();
            }
            builder.bindComponent(type, client, clientItf, server, serverItf, context);
        }

        public Object getResult() {
            return null;
        }

        public void setResult(final Object result) {
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[BindTask(" + clientItf + "," + serverItf + ")]";
        }
    }
}
