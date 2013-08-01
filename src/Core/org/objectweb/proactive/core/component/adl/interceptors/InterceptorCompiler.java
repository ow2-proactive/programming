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
package org.objectweb.proactive.core.component.adl.interceptors;

import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.task.core.Task;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.fractal.task.deployment.lib.AbstractConfigurationTask;
import org.objectweb.proactive.core.component.adl.types.PATypeInterface;
import org.objectweb.proactive.core.component.interception.Interceptor;


/**
 * A {@link PrimitiveCompiler} to compile tasks in charge of adding {@link Interceptor interceptors}
 * to functional interfaces.
 *
 * @author The ProActive Team
 */
public class InterceptorCompiler implements PrimitiveCompiler, BindingController {
    /**
     * Name of the mandatory interface bound to the {@link InterceptorBuilder} used by this compiler.
     */
    public final static String BUILDER_BINDING = "builder";

    /**
     * The {@link InterceptorBuilder} used by this compiler.
     */
    public InterceptorBuilder builder;

    /**
     * {@inheritDoc}
     */
    @Override
    public void compile(List<ComponentContainer> path, ComponentContainer container, TaskMap tasks,
            Map<Object, Object> context) throws ADLException {
        if (container instanceof InterfaceContainer) {
            for (Interface itf : ((InterfaceContainer) container).getInterfaces()) {
                String interceptorsAttributeValue = itf.astGetAttributes().get(
                        PATypeInterface.INTERCEPTORS_ATTRIBUTE_NAME);

                if (interceptorsAttributeValue != null) {
                    TaskMap.TaskHole createTaskHole = tasks.getTaskHole("create", container);
                    TaskMap.TaskHole startTaskHole = tasks.getTaskHole("start", container);
                    String[] interceptors = interceptorsAttributeValue.split(",");

                    for (String interceptor : interceptors) {
                        String interceptorID = null;

                        if (interceptor.startsWith("this.")) { // Controller interface
                            interceptorID = interceptor.substring(interceptor.indexOf('.') + 1);
                        } else { // Server interface of a NF component
                            interceptorID = interceptor;
                        }

                        InterceptorTask interceptorTask = new InterceptorTask(this.builder, itf.getName(),
                            interceptorID);
                        TaskMap.TaskHole interceptorTaskHole = tasks.addTask("interceptor" + itf.getName() +
                            interceptorID, container, interceptorTask);

                        interceptorTask.setInstanceProviderTask(createTaskHole);
                        interceptorTask.addDependency(createTaskHole, Task.PREVIOUS_TASK_ROLE, context);

                        // Component can't be started before interceptor task is done
                        startTaskHole.addDependency(interceptorTaskHole, Task.PREVIOUS_TASK_ROLE, context);
                    }
                }
            }
        }
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.equals(BUILDER_BINDING)) {
            this.builder = (InterceptorBuilder) value;
        }
    }

    public String[] listFc() {
        return new String[] { BUILDER_BINDING };
    }

    public Object lookupFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            return this.builder;
        }
        return null;
    }

    public void unbindFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            this.builder = null;
        }
    }

    static class InterceptorTask extends AbstractConfigurationTask {
        private InterceptorBuilder builder;

        private String interfaceName;

        private String interceptorID;

        public InterceptorTask(final InterceptorBuilder builder, final String interfaceName,
                final String interceptorID) {
            this.builder = builder;
            this.interfaceName = interfaceName;
            this.interceptorID = interceptorID;
        }

        @Override
        public void execute(Map<Object, Object> context) throws Exception {
            Object component = getInstanceProviderTask().getInstance();

            this.builder.addInterceptor(component, this.interfaceName, this.interceptorID);
        }

        @Override
        public Object getResult() {
            return null;
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[InterceptorTask(" + this.interfaceName + "," +
                this.interceptorID + ")]";
        }
    }
}
