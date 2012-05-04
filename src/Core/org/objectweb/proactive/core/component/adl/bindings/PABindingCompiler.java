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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.bindings.Binding;
import org.objectweb.fractal.adl.bindings.BindingBuilder;
import org.objectweb.fractal.adl.bindings.BindingCompiler;
import org.objectweb.fractal.adl.bindings.BindingContainer;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.ComponentPair;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.task.core.Task;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.fractal.task.core.TaskMap.TaskHole;
import org.objectweb.fractal.task.deployment.api.InstanceProviderTask;
import org.objectweb.fractal.task.deployment.lib.AbstractRequireInstanceProviderTask;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The {@link PABindingCompiler} extends the Fractal's {@link BindingCompiler} to consider
 * multicast bindings, and to create bindings in the membrane and bindings between
 * NF interfaces.
 * 
 * 
 * @author The ProActive Team
 *
 */
public class PABindingCompiler extends BindingCompiler {
    private static Map<String, Integer> multicastItfTaskIndexer = new HashMap<String, Integer>();
    protected static final Logger loggerADL = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    // --------------------------------------------------------------------------
    // Implementation of the Compiler interface
    // --------------------------------------------------------------------------
    @Override
    public void compile(final List path, final ComponentContainer container,
            final org.objectweb.fractal.task.core.TaskMap tasks, final Map context) throws ADLException {

        Map<String, ComponentContainer> fSubComponents = new HashMap<String, ComponentContainer>();
        Map<String, ComponentContainer> nfSubComponents = new HashMap<String, ComponentContainer>();

        // collects the F subcomponents
        fSubComponents.put("this", container);
        Component[] comps = container.getComponents();
        for (Component comp : comps) {
            fSubComponents.put(comp.getName(), comp);
        }

        // collects the NF subcomponents
        nfSubComponents.put("this", container);
        if (container instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) container).getController();
            if (ctrl != null) {
                if (ctrl instanceof ComponentContainer) {
                    Component[] nfComps = ((ComponentContainer) ctrl).getComponents();
                    for (Component nfComp : nfComps) {
                        nfSubComponents.put(nfComp.getName(), nfComp);
                    }
                }
            }
        }

        //TODO This way of identifying the Multicast interfaces should be optimized.
        //     We're searching several times the same interface in the try and in the catch

        // process the F bindings
        if (container instanceof BindingContainer) {
            Binding[] bindings = ((BindingContainer) container).getBindings();
            for (Binding binding : bindings) {

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

                // obtains the 'from' component and retrieves its 'create' task
                String value = binding.getFrom();
                int index = value.indexOf('.');
                Object clientComp = fSubComponents.get(value.substring(0, index));
                String clientItf = value.substring(index + 1);
                TaskHole createClientTask = tasks.getTaskHole("create", clientComp);

                // obtains the 'to' component and retrieves its 'create' task
                value = binding.getTo();
                Object serverComp = null;
                String serverItf = null;
                TaskHole createServerTask = null;
                if (type != PABindingBuilder.WEBSERVICE_BINDING) {
                    index = value.indexOf('.');
                    serverComp = fSubComponents.get(value.substring(0, index));
                    serverItf = value.substring(index + 1);
                    createServerTask = tasks.getTaskHole("create", serverComp);
                } else {
                    serverItf = value;
                }

                try {
                    // the task may already exist, in case of a shared component
                    // but multicast interfaces are handled specifically
                    // Verify if the interfaces involved are multicast ... if they are, throw an Exception to use an special treatment
                    if ((BindingBuilder.EXPORT_BINDING == type) && (container instanceof BindingContainer) &&
                        (container instanceof InterfaceContainer)) {
                        // check interfaces of "this"
                        Interface[] itfs = ((InterfaceContainer) container).getInterfaces();
                        for (int j = 0; j < itfs.length; j++) {
                            TypeInterface itf = (TypeInterface) itfs[j];
                            // if multicast, treat it specially
                            if (clientItf.equals(itf.getName())) {
                                if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
                                    throw new NoSuchElementException(clientItf);
                                }
                            }
                        }
                    } else if ((clientComp instanceof BindingContainer) &&
                        (clientComp instanceof InterfaceContainer)) {
                        // check interface of 'clientComp' (which maybe still 'this'?)
                        Interface[] itfs = ((InterfaceContainer) clientComp).getInterfaces();
                        for (int j = 0; j < itfs.length; j++) {
                            TypeInterface itf = (TypeInterface) itfs[j];
                            // if multicast, treat it specially
                            if (clientItf.equals(itf.getName())) {
                                if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
                                    throw new NoSuchElementException(clientItf);
                                }
                            }
                        }
                    }
                    // get the "bindTask" identified by 'bind+clientItf' and the clientComponent
                    // if it has not been created yet (highly probable at this step, because there are not shared component in GCM),
                    // it will throw a NoSuchElementException
                    tasks.getTask("bind" + clientItf, clientComp);
                } catch (NoSuchElementException e) {
                    // Create the PABindTask, using the client and server interfaces, 
                    // and the respective 'create' tasks as provider tasks
                    PABindTask bindTask = new PABindTask((PABindingBuilderItf) builder, type, clientItf,
                        serverItf);
                    bindTask.setInstanceProviderTask(createClientTask);
                    bindTask.setServerInstanceProviderTask(createServerTask);
                    loggerADL.debug("[PABindingCompiler] Creating PABindTask: " + clientComp + "." +
                        clientItf + " ---> " + serverComp + "." + serverItf);

                    // Add an index to the client interface name in case of a multicast binding.
                    // This is done to be able to have another 'bindTask' for the same client interface.
                    // Otherwise, the new added task will overwrite the previous one in the TaskMap
                    if ((BindingBuilder.EXPORT_BINDING == type) && (container instanceof BindingContainer) &&
                        (container instanceof InterfaceContainer)) {
                        // export binding : use current container
                        InterfaceContainer itfContainer = ((InterfaceContainer) container);
                        clientItf = setMulticastIndex(clientComp, clientItf, itfContainer);
                    } else if ((clientComp instanceof BindingContainer) &&
                        (clientComp instanceof InterfaceContainer)) {
                        // normal or import binding : use client component as container
                        InterfaceContainer itfContainer = ((InterfaceContainer) clientComp);
                        clientItf = setMulticastIndex(clientComp, clientItf, itfContainer);
                    }

                    // add the create task in the TaskHole identified by 'bind+clientItf' and the client component
                    TaskHole bindTaskHole = tasks.addTask("bind" + clientItf, clientComp, bindTask);

                    // if the client component is not 'this', 
                    // then make sure that the client component is added to 'this' component before.
                    // i.e., set the addTask as a 'previous' dependency
                    if (clientComp != container) {
                        TaskHole addTask = tasks.getTaskHole("add", new ComponentPair(container,
                            (Component) clientComp));
                        bindTask.addDependency(addTask, Task.PREVIOUS_TASK_ROLE, context);
                        //bindTask.addPreviousTask(addTask);
                    }
                    // the server component is not "this" (and it's not a WS-Binding either),
                    // make sure that the server component is added to 'this' component before
                    if ((type != PABindingBuilder.WEBSERVICE_BINDING) && (serverComp != container)) {
                        TaskHole addTask = tasks.getTaskHole("add", new ComponentPair(container,
                            (Component) serverComp));
                        bindTask.addDependency(addTask, Task.PREVIOUS_TASK_ROLE, context);
                        //bindTask.addPreviousTask(addTask);
                    }

                    // obtain the 'start' task of the client component and set the created BindTask
                    // as a 'previous' dependency for the startTask
                    TaskHole startTask = tasks.getTaskHole("start", clientComp);
                    startTask.addDependency(bindTaskHole, Task.PREVIOUS_TASK_ROLE, context);
                    //startTask.addPreviousTask(bindTask);
                }
            }
        }

        // process the NF bindings
        if (container instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) container).getController();
            if (ctrl != null) {
                if (ctrl instanceof BindingContainer) {
                    Binding[] nfBindings = ((BindingContainer) ctrl).getBindings();
                    for (Binding binding : nfBindings) {

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
                        // add another type of bindings: MEMBRANE_BINDING from membrane.server to membrane.client
                        if (binding.getFrom().startsWith("this.") && binding.getTo().startsWith("this.")) {
                            type = PABindingBuilder.MEMBRANE_BINDING;
                        }

                        loggerADL.debug("[PABindingCompiler] NFBinding " + binding.getFrom() + " ---> " +
                            binding.getTo());

                        // remember: NF interfaces must end with "-controller"

                        // obtains the 'from' component and retrieves its 'create' task
                        String value = binding.getFrom();
                        int index = value.indexOf('.');
                        String clientCompName = value.substring(0, index);
                        String clientItfName = value.substring(index + 1);
                        Object clientComp = null;
                        if (clientItfName.endsWith("-controller")) {
                            clientComp = fSubComponents.get(clientCompName);
                        } else {
                            clientComp = nfSubComponents.get(clientCompName);
                        }
                        // if clientComp == null, we should throw and ADLException "Component not found"
                        TaskHole createClientTask = tasks.getTaskHole("create", clientComp);

                        // obtains the 'to' component and retrieves its 'create' task
                        value = binding.getTo();
                        String serverCompName = null;
                        String serverItfName = null;
                        Object serverComp = null;
                        TaskHole createServerTask = null;
                        if (type != PABindingBuilder.WEBSERVICE_BINDING) {
                            index = value.indexOf('.');
                            serverCompName = value.substring(0, index);
                            serverItfName = value.substring(index + 1);
                            if (serverItfName.endsWith("-controller")) {
                                serverComp = fSubComponents.get(serverCompName);
                            } else {
                                serverComp = nfSubComponents.get(serverCompName);
                            }
                            // if serverComp == null, we should throw and ADLException "Component not found"
                            createServerTask = tasks.getTaskHole("create", serverComp);
                        } else {
                            serverItfName = value;
                        }

                        // obtains the 'membraneOwner' component (i.e. the 'container') and retrives its 'create' task
                        Object membraneOwner = container;
                        TaskHole createMembraneOwnerTask = tasks.getTaskHole("create", membraneOwner);

                        try {
                            // the task may already exist, in case of a shared component
                            // but multicast interfaces are handled specifically
                            // Verify if the interfaces involved are multicast ... if they are, throw and Exception to use an special treatment
                            if ((BindingBuilder.EXPORT_BINDING == type) &&
                                (ctrl instanceof BindingContainer) && (ctrl instanceof InterfaceContainer)) {
                                // check interfaces of "this"
                                Interface[] itfs = ((InterfaceContainer) ctrl).getInterfaces();
                                for (int j = 0; j < itfs.length; j++) {
                                    TypeInterface itf = (TypeInterface) itfs[j];
                                    // if multicast, treat it specially
                                    if (clientItfName.equals(itf.getName())) {
                                        if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
                                            throw new NoSuchElementException(clientItfName);
                                        }
                                    }
                                }
                            } else if ((clientComp instanceof BindingContainer) &&
                                (clientComp instanceof InterfaceContainer)) {
                                // check interface of 'clientComp' (which maybe still 'this'?)
                                Interface[] itfs = ((InterfaceContainer) clientComp).getInterfaces();
                                for (int j = 0; j < itfs.length; j++) {
                                    TypeInterface itf = (TypeInterface) itfs[j];
                                    // if multicast, treat it specially
                                    if (clientItfName.equals(itf.getName())) {
                                        if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(itf.getCardinality())) {
                                            throw new NoSuchElementException(clientItfName);
                                        }
                                    }
                                }
                            }
                            // get the "bindTask" identified by 'bind+clientItf' and the clientComponent
                            // if it has not been created yet (highly probable at this step, because there are not shared component in GCM),
                            // it will throw a NoSuchElementException
                            tasks.getTask("bind" + clientItfName, clientComp);
                        } catch (NoSuchElementException e) {
                            // Create the PABindTask, using the client and server interfaces, 
                            // and the respective 'create' tasks as provider tasks
                            PABindTask bindTask = new PABindTask((PABindingBuilderItf) builder, type,
                                clientItfName, serverItfName, false);
                            bindTask.setInstanceProviderTask(createClientTask);
                            bindTask.setServerInstanceProviderTask(createServerTask);
                            bindTask.setMembraneOwnerInstanceProviderTask(createMembraneOwnerTask);
                            loggerADL.debug("[PABindingCompiler] Creating NF PABindTask: " + clientComp +
                                "." + clientItfName + " ---> " + serverComp + "." + serverItfName);

                            // Add an index to the client interface name in case of a multicast binding.
                            // This is done to be able to have another 'bindTask' for the same client interface.
                            // Otherwise, the new added task will overwrite the previous one in the TaskMap
                            if ((BindingBuilder.EXPORT_BINDING == type) &&
                                (ctrl instanceof BindingContainer) && (ctrl instanceof InterfaceContainer)) {
                                // export binding : use current container
                                InterfaceContainer itfContainer = ((InterfaceContainer) ctrl);
                                clientItfName = setMulticastIndex(clientComp, clientItfName, itfContainer);
                            } else if ((clientComp instanceof BindingContainer) &&
                                (clientComp instanceof InterfaceContainer)) {
                                // normal or import binding : use client component as container
                                InterfaceContainer itfContainer = ((InterfaceContainer) clientComp);
                                clientItfName = setMulticastIndex(clientComp, clientItfName, itfContainer);
                            }

                            // add the create task in the TaskHole identified by 'bind+clientItf' and the client component
                            TaskHole bindTaskHole = tasks.addTask("bind" + clientItfName, clientComp,
                                    bindTask);

                            // if the client component is not 'this', 
                            // then make sure that the client component is added to 'this' component before.
                            // i.e., set the addTask as a 'previous' dependency
                            if (clientComp != container) {
                                TaskHole addTask = tasks.getTaskHole("add", new ComponentPair(container,
                                    (Component) clientComp));
                                bindTask.addDependency(addTask, Task.PREVIOUS_TASK_ROLE, context);
                                //bindTask.addPreviousTask(addTask);
                            }
                            // the server component is not "this" (and it's not a WS-Binding either),
                            // make sure that the server component is added to 'this' component before
                            if ((type != PABindingBuilder.WEBSERVICE_BINDING) && (serverComp != container)) {
                                TaskHole addTask = tasks.getTaskHole("add", new ComponentPair(container,
                                    (Component) serverComp));
                                bindTask.addDependency(addTask, Task.PREVIOUS_TASK_ROLE, context);
                                //bindTask.addPreviousTask(addTask);
                            }

                            // obtain the 'start' task of the client component and set the created BindTask
                            // as a 'previous' dependency for the startTask
                            TaskHole startTask = tasks.getTaskHole("start", clientComp);
                            startTask.addDependency(bindTaskHole, Task.PREVIOUS_TASK_ROLE, context);
                            //startTask.addPreviousTask(bindTask);

                        }

                    }
                }
            }
        }
    }

    /**
     * In the case that the client interface is multicast, this method modifies the name
     * of the client interface by adding an index to it.
     * 
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
                    // If multicast interface, then multiple bindings are allowed from this interface.
                    // Add an incremental index to the name, to be able to add several task entries to the TaksMap
                    // (otherwise the next one will overwrite the previous one)
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

    //private TypeInterface getInterface(InterfaceContainer itfContainer, String itfName) {

    //}

    static class PABindTask extends AbstractRequireInstanceProviderTask {
        private TaskMap.TaskHole serverInstanceProviderTask = null;
        private TaskMap.TaskHole membraneOwnerInstanceProviderTask = null;

        private PABindingBuilderItf builder;
        private int type;
        private String clientItf;
        private String serverItf;
        private boolean isFunctional;

        public PABindTask(final PABindingBuilderItf builder, final int type, final String clientItf,
                final String serverItf) {
            this.builder = builder;
            this.type = type;
            this.clientItf = clientItf;
            this.serverItf = serverItf;
            this.isFunctional = true;
        }

        public PABindTask(final PABindingBuilderItf builder, final int type, final String clientItf,
                final String serverItf, boolean isFunctional) {
            this.builder = builder;
            this.type = type;
            this.clientItf = clientItf;
            this.serverItf = serverItf;
            this.isFunctional = isFunctional;
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

        public InstanceProviderTask getMembraneOwnerInstanceProviderTask() {
            return (membraneOwnerInstanceProviderTask) == null ? null
                    : (InstanceProviderTask) membraneOwnerInstanceProviderTask.getTask();
        }

        public void setMembraneOwnerInstanceProviderTask(final TaskMap.TaskHole task) {
            if (membraneOwnerInstanceProviderTask != null) {
                removePreviousTask(membraneOwnerInstanceProviderTask);
            }
            membraneOwnerInstanceProviderTask = task;
            if (membraneOwnerInstanceProviderTask != null) {
                addPreviousTask(membraneOwnerInstanceProviderTask);
            }
        }

        public void execute(final Map<Object, Object> context) throws Exception {
            Object client = null;
            Object server = null;
            Object membraneOwner = null;
            if (type != PABindingBuilder.WEBSERVICE_BINDING) {
                client = getInstanceProviderTask().getInstance();
                server = getServerInstanceProviderTask().getInstance();
            } else {
                client = getInstanceProviderTask().getInstance();
            }
            // get the membrane owner, if needed
            if (getMembraneOwnerInstanceProviderTask() != null) {
                membraneOwner = getMembraneOwnerInstanceProviderTask().getInstance();
            }

            loggerADL.debug("[PABindingCompiler] Executing " + (isFunctional ? "F" : "NF") + " binding: " +
                clientItf + " ---> " + serverItf);
            builder.bindComponent(type, client, clientItf, server, serverItf, membraneOwner, isFunctional,
                    context);

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
