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
package org.objectweb.proactive.core.component.adl.implementations;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.implementations.Implementation;
import org.objectweb.fractal.adl.implementations.ImplementationCompiler;
import org.objectweb.fractal.adl.implementations.ImplementationContainer;
import org.objectweb.fractal.adl.nodes.VirtualNodeContainer;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.fractal.task.core.TaskMap.TaskHole;
import org.objectweb.fractal.task.deployment.lib.AbstractInstanceProviderTask;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The {@link PAImplementationCompiler} compiles the &lt;component&gt; nodes and
 * creates the creation tasks {@link CreateTask} for each component.<br/>
 * 
 * For each component, the {@link PAImplementationCompiler} collects the info required 
 * to create it:
 * <ul>
 *   <li>name</li>
 *   <li>implementing class</li>
 *   <li>controller descriptions</li>
 *   <li>virtual nodes</li>
 *   <li>hierarchy</li>
 *   <li>functional/non-functional quality</li>,
 * </ul>
 * and creates the {@link CreateTask} that handles the component creation.
 * 
 * @author The ProActive Team
 */
public class PAImplementationCompiler extends ImplementationCompiler {

    // Is this counter is used???
    //protected static int counter = 0;

    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    @Override
    public void compile(List<ComponentContainer> path, ComponentContainer container, TaskMap tasks,
            Map<Object, Object> context) throws ADLException {

        //DEBUG
        String name = null;
        boolean f = true;
        if (container instanceof Definition) {
            name = ((Definition) container).getName();
        } else if (container instanceof Component) {
            name = ((Component) container).getName();
        }
        f = (container.astGetDecoration("NF") == null);
        logger.debug("[PAImplementationCompiler] Compiling " + (f ? "F" : "NF") + " component: " + name);
        //--DEBUG

        // collect info required for creating the component
        ObjectsContainer obj = init(path, container, tasks, context);
        // determines content description and controller description info
        controllers(obj.getImplementation(), obj.getController(), obj.getName(), obj);
        // create the task that will be in charge of creating the component
        end(tasks, container, context, obj.getName(), obj.getDefinition(), obj.getControllerDesc(), obj
                .getContentDesc(), obj.getVn(), obj.isFunctional());
    }

    protected static String getControllerPath(String controller, String name) {
        URL controllerURL = PAImplementationCompiler.class.getResource(controller);
        if (controllerURL != null) {
            return controllerURL.getPath();
        } else {
            logger.warn("Can't retrieve controller description \"" + controller + "\" for component " + name);
            return null;
        }
    }

    /**
     * Collects info for the creation of the component: implementation class, controller descriptor name, component name,
     * definition, virtual node, the number of subcomponents (if any), and determines if it is a functional or non-functional
     * component.
     * 
     * @param path
     * @param container
     * @param tasks
     * @param context
     * @return An ObjectsContainer object including all the collected info.
     */
    protected ObjectsContainer init(final List<ComponentContainer> path, final ComponentContainer container,
            final TaskMap tasks, final Map<Object, Object> context) {

        //counter++; // it seems that this counter is not used ...

        String implementation = null;
        if (container instanceof ImplementationContainer) {
            ImplementationContainer ic = (ImplementationContainer) container;
            Implementation i = ic.getImplementation();
            if (i != null) {
                implementation = i.getClassName();
            }
        }

        String controller = null;
        if (container instanceof ControllerContainer) {
            ControllerContainer cc = (ControllerContainer) container;
            if (cc.getController() != null) {
                controller = cc.getController().getDescriptor();
            }
        }

        String name = null;
        if (container instanceof Definition) {
            name = ((Definition) container).getName();
        } else if (container instanceof Component) {
            name = ((Component) container).getName();
        }

        String definition = null;
        if (container instanceof Definition) {
            definition = name;
        } else {
            definition = (String) ((Node) container).astGetDecoration("definition");
        }

        VirtualNode n = null;
        if (container instanceof VirtualNodeContainer) {
            try {
                n = (VirtualNode) ((VirtualNodeContainer) container).getVirtualNode();
            } catch (ClassCastException e) {
                throw new ProActiveRuntimeException(
                    "DOCTYPE definition should be the following when using ProActive : \n"
                        + "<!DOCTYPE definition PUBLIC \"-//objectweb.org//DTD Fractal ADL 2.0//EN\" \"classpath://org/objectweb/proactive/core/component/adl/xml/proactive.dtd\">");
            }

            if (n == null) {
                // see Leclerq modification request : try to find a vn specified
                // in a parent component
                for (int i = path.size() - 1; i >= 0; --i) {
                    if (path.get(i) instanceof VirtualNodeContainer) {
                        try {
                            n = (VirtualNode) ((VirtualNodeContainer) path.get(i)).getVirtualNode();

                            if (n != null) {
                                break;
                            }
                        } catch (ClassCastException e) {
                            throw new ProActiveRuntimeException(
                                "DOCTYPE definition should be the following when using ProActive : \n"
                                    + "<!DOCTYPE definition PUBLIC \"-//objectweb.org//DTD Fractal ADL 2.0//EN\" \"classpath://org/objectweb/proactive/core/component/adl/xml/proactive.dtd\">");
                        }
                    }
                }
            }
        }

        // check if this Component has subcomponents
        int k = container.getComponents().length;

        // determine if the component is functional
        boolean isFunctional = (container.astGetDecoration("NF") == null);

        // return the object with all the info collected
        return new ObjectsContainer(implementation, controller, name, definition, n, k > 0, isFunctional);
    }

    /** 
     * Completes the collected ObjectsContainer with the ContentDescription and the ControllerDescription objects.<br/><br/>
     * 
     * Determines the hierarchical type (composite/primitive) by checking if the component has 
     * subcomponents or not. In fact, a composite may have an implementation class, in case that
     * the composite must provide an AttributeController interface (so, checking that implementation==null
     * is not enough).
     * 
     * @param implementation
     * @param controller
     * @param name
     * @param obj
     */
    private void controllers(String implementation, String controller, String name, ObjectsContainer obj) {
        ContentDescription contentDesc = null;
        ControllerDescription controllerDesc = null;

        if (implementation == null) {
            // a composite component without attributes
            if ("composite".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE);
                contentDesc = new ContentDescription(Composite.class.getName());
            } else {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE, getControllerPath(
                        controller, name));
            }

        } else if (obj.hasSubcomponents()) {
            // a composite component with attributes 
            //    in that case it must have an Attributes node, and the class implementation must implement
            //    the Attributes signature
            contentDesc = new ContentDescription(implementation);

            // treat it as a composite
            if ("composite".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE);
            } else {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE, getControllerPath(
                        controller, name));
            }

        } else {
            // a primitive component
            contentDesc = new ContentDescription(implementation);

            if ("primitive".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.PRIMITIVE);
            } else {
                controllerDesc = new ControllerDescription(name, Constants.PRIMITIVE, getControllerPath(
                        controller, name));
            }
        }

        // update the ObjectsContainer object
        obj.setContentDesc(contentDesc);
        obj.setControllerDesc(controllerDesc);
    }

    /**
     * Creates the {@link CreateTask} that will take charge of creating the component,
     * and sets its dependency (the corresponding task of type "type").
     * 
     * @param tasks
     * @param container
     * @param context
     * @param name
     * @param definition
     * @param controllerDesc
     * @param contentDesc
     * @param n
     * @param isFunctional
     */
    protected void end(final TaskMap tasks, final ComponentContainer container,
            final Map<Object, Object> context, String name, String definition,
            ControllerDescription controllerDesc, ContentDescription contentDesc, VirtualNode n,
            boolean isFunctional) {
        AbstractInstanceProviderTask createTask = null;

        // the CreateTask delegates the effective creation to a PAImplementationBuilder
        createTask = new CreateTask((PAImplementationBuilder) builder, container, name, definition,
            controllerDesc, contentDesc, n, context, isFunctional);

        // obtains the TaskHole of type "type" associated to this container (which may be null if it has not yet been created)
        // and sets it as the 'FactoryProvider' Task for this task. This implies that the factory provider task will be executed
        // before the current task.
        // Finally, it adds the current task, as a task of type "create", associate to this container.
        TaskHole typeTask = tasks.getTaskHole("type", container);
        createTask.setFactoryProviderTask(typeTask);
        tasks.addTask("create", container, createTask);
    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    // TODO change visibility of this inner class in ImplementationCompiler
    static class CreateTask extends AbstractInstanceProviderTask {
        PAImplementationBuilder builder;
        String name;
        String definition;
        ControllerDescription controllerDesc;
        ContentDescription contentDesc;
        VirtualNode vn;
        Map<Object, Object> context;
        ComponentContainer container;
        boolean functional = true;

        public CreateTask(final PAImplementationBuilder builder, final ComponentContainer container,
                final String name, final String definition, final ControllerDescription controllerDesc,
                final ContentDescription contentDesc, final VirtualNode vn,
                final Map<Object, Object> context, final boolean isFunctional) {
            this.builder = builder;
            this.container = container;
            this.name = name;
            this.definition = definition;
            this.controllerDesc = controllerDesc;
            this.contentDesc = contentDesc;
            this.vn = vn;
            this.context = context;
            this.functional = isFunctional;
        }

        public void execute(Map<Object, Object> context) throws Exception {
            if (getInstance() != null) {
                return;
            }
            logger.debug("[PAImplementationCompiler] Executing creation of " + (functional ? "F" : "NF") +
                " component" + name);
            Object type = getFactoryProviderTask().getFactory();
            Object result = builder.createComponent(type, name, definition, controllerDesc, contentDesc, vn,
                    functional, context);
            setInstance(result);
        }

        @Override
        public String toString() {
            return "T" + System.identityHashCode(this) + "[CreateTask(" + name + "," + controllerDesc + "," +
                contentDesc + ")]";
        }
    }

    protected class ObjectsContainer {
        private String implementation;
        private String controller;
        private String name;
        private String definition;
        private VirtualNode n;
        ContentDescription contentDesc = null;
        ControllerDescription controllerDesc = null;
        private boolean subcomponents = false;
        private boolean functional = true;

        public ObjectsContainer(String implementation, String controller, String name, String definition,
                VirtualNode n, boolean subcomponents, boolean isFunctional) {
            this.implementation = implementation;
            this.controller = controller;
            this.name = name;
            this.definition = definition;
            this.n = n;
            this.subcomponents = subcomponents;
            this.functional = isFunctional;
        }

        public String getController() {
            return controller;
        }

        public String getDefinition() {
            return definition;
        }

        public String getImplementation() {
            return implementation;
        }

        public VirtualNode getVn() {
            return n;
        }

        public String getName() {
            return name;
        }

        public ContentDescription getContentDesc() {
            return contentDesc;
        }

        public void setContentDesc(ContentDescription contentDesc) {
            this.contentDesc = contentDesc;
        }

        public ControllerDescription getControllerDesc() {
            return controllerDesc;
        }

        public void setControllerDesc(ControllerDescription controllerDesc) {
            this.controllerDesc = controllerDesc;
        }

        public boolean hasSubcomponents() {
            return subcomponents;
        }

        public boolean isFunctional() {
            return functional;
        }

    }
}
