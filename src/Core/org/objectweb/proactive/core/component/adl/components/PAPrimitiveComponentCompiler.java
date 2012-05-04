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
package org.objectweb.proactive.core.component.adl.components;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.ComponentPair;
import org.objectweb.fractal.adl.components.PrimitiveComponentCompiler;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.task.core.Task;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.fractal.task.deployment.api.InstanceProviderTask;
import org.objectweb.fractal.task.deployment.lib.AbstractInitializationTask;
import org.objectweb.fractal.task.deployment.lib.AbstractRequireInstanceProviderTask;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The {@link PAPrimitiveComponentCompiler} extends Fractal's {@link PrimitiveComponentCompiler}
 * to create the composition tasks.<br/><br/>
 * 
 * An {@link AddTask} is created for each subcomponent (functional content),
 * and for each component in the membrane (non-functional content).<br/><br/>
 * 
 * The {@link StartTask} takes charge of starting the membrane
 * after all the inside manipulations have been done.
 * 
 * 
 * @author The ProActive Team
 *
 */
public class PAPrimitiveComponentCompiler extends PrimitiveComponentCompiler {

    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    /**
     * Creates a StartTask for the component to be executed after the CreateTask and the AddTask.<br/><br/>
     * 
     * Creates an AddTask for each F subcomponent, and for each NF component in the membrane.
     */
    @Override
    public void compile(List path, ComponentContainer container, TaskMap tasks, Map context)
            throws ADLException {

        boolean isFunctional = (container.astGetDecoration("NF") == null);
        logger.debug("[PAPrimitiveComponentCompiler] Compiling " + (isFunctional ? "F" : "NF") +
            " component " + container.toString());

        // the "create" task for this container
        TaskMap.TaskHole createTaskHole = tasks.getTaskHole("create", container);

        // create the StartTask (which, so far, does nothing), and set the "create" task as provider of this one
        StartTask startTask = new StartTask((PAComponentBuilderItf) builder);
        startTask.setInstanceProviderTask(createTaskHole);
        tasks.addTask("start", container, startTask);

        // set the "create" task as a BEFORE-dependency of the "start" task
        startTask.addDependency(createTaskHole, Task.PREVIOUS_TASK_ROLE, context);

        // create an AddTask for each subcomponent
        Component[] comps = container.getComponents();
        for (int i = 0; i < comps.length; i++) {

            logger.debug("[PAPrimitiveComponentCompiler] --> Add component " + comps[i].getName());
            // the "create" task for the subcomponent
            TaskMap.TaskHole createSubComponentTaskHole = tasks.getTaskHole("create", comps[i]);

            // the ComponentPair works as "unique identifier" for the AddTask
            ComponentPair pair = new ComponentPair(container, comps[i]);

            try {
                // the task may already exist, in case of a shared component
                tasks.getTask("add", pair);
            } catch (NoSuchElementException e) {

                AddTask addTask = new AddTask((PAComponentBuilderItf) builder, comps[i].getName(), true);
                // the respective "create" tasks must execute before the "add" tasks
                addTask.setInstanceProviderTask(createTaskHole);
                addTask.setSubInstanceProviderTask(createSubComponentTaskHole);

                // add the "add" task
                TaskMap.TaskHole addTaskHole = tasks.addTask("add", pair, addTask);

                // set the "create" tasks as dependencies of the "add" task
                addTask.addDependency(createTaskHole, Task.PREVIOUS_TASK_ROLE, context);
                addTask.addDependency(createSubComponentTaskHole, Task.PREVIOUS_TASK_ROLE, context);

                // set the "add" task as a dependency of the "start" task
                startTask.addDependency(addTaskHole, Task.PREVIOUS_TASK_ROLE, context);
            }

        }

        // create an AddTask for each NF component that must go in the membrane
        if (container instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) container).getController();
            if (ctrl != null) {
                if (ctrl instanceof ComponentContainer) {
                    Component[] nfComps = ((ComponentContainer) ctrl).getComponents();

                    for (Component nfComp : nfComps) {
                        logger.debug("[PAPrimitiveComponentCompiler] --> Add component " + nfComp.getName());
                        TaskMap.TaskHole createSubComponentTaskHole = tasks.getTaskHole("create", nfComp);

                        ComponentPair pair = new ComponentPair(container, nfComp);
                        try {
                            // the task may already exist, in case of a shared component
                            tasks.getTask("add", pair);
                        } catch (NoSuchElementException e) {

                            AddTask addTask = new AddTask((PAComponentBuilderItf) builder, nfComp.getName(),
                                false);
                            // the respective "create" tasks must execute before the "add" tasks							
                            addTask.setInstanceProviderTask(createTaskHole);
                            addTask.setSubInstanceProviderTask(createSubComponentTaskHole);

                            // add the "add" task
                            TaskMap.TaskHole addTaskHole = tasks.addTask("add", pair, addTask);

                            // set the "create" tasks as dependencies of the "add" task
                            addTask.addDependency(createTaskHole, Task.PREVIOUS_TASK_ROLE, context);
                            addTask.addDependency(createSubComponentTaskHole, Task.PREVIOUS_TASK_ROLE,
                                    context);

                            // set the "add" task as a dependency of the "start" task
                            startTask.addDependency(addTaskHole, Task.PREVIOUS_TASK_ROLE, context);
                        }
                    }
                }
            }
        }

    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    /**
     * The {@link AddTask} takes charge of the inclusion of subcomponents inside the functional content
     * of composite. Here, it has also been extended to include the NF components inside the membrane.
     */
    static class AddTask extends AbstractRequireInstanceProviderTask {

        private PAComponentBuilderItf builder;

        private TaskMap.TaskHole subInstanceProviderTask;

        private String name;

        private boolean isFunctional;

        public AddTask(final PAComponentBuilderItf builder, final String name, final boolean isFunctional) {
            this.builder = builder;
            this.name = name;
            this.isFunctional = isFunctional;
        }

        public InstanceProviderTask getSubInstanceProviderTask() {
            return (subInstanceProviderTask) == null ? null : (InstanceProviderTask) subInstanceProviderTask
                    .getTask();
        }

        public void setSubInstanceProviderTask(final TaskMap.TaskHole task) {
            if (subInstanceProviderTask != null) {
                removePreviousTask(subInstanceProviderTask);
            }
            subInstanceProviderTask = task;
            if (subInstanceProviderTask != null) {
                addPreviousTask(subInstanceProviderTask);
            }
        }

        public void execute(final Map<Object, Object> context) throws Exception {
            Object parent = getInstanceProviderTask().getInstance();
            Object child = getSubInstanceProviderTask().getInstance();
            builder.addComponent(parent, child, name, isFunctional, context);
        }

        public Object getResult() {
            return null;
        }

        public void setResult(Object result) {
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[AddTask(" + name + ")]";
        }
    }

    /**
     * The {@link StartTask} task is supposed to start a component.
     * However the corresponding call to the {@link PAComponentBuilder} does not do anything
     * (it was the same for Fractal's {@link FractalComponentBuilder}).
     * 
     * The {@link StartTask} task is used here to start the Membrane of the component, so that the
     * life cycle can be started later.
     * 
     */
    static class StartTask extends AbstractInitializationTask {

        private PAComponentBuilderItf builder;

        public StartTask(final PAComponentBuilderItf builder) {
            this.builder = builder;
        }

        public void execute(final Map<Object, Object> context) throws Exception {
            // this starts the membrane
            builder.startMembrane(getInstanceProviderTask().getInstance(), context);
            // this does nothing
            builder.startComponent(getInstanceProviderTask().getInstance(), context);

        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[StartTask()]";
        }
    }

}
