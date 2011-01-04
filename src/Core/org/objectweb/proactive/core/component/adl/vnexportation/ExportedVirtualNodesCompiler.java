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
package org.objectweb.proactive.core.component.adl.vnexportation;

import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.adl.nodes.VirtualNodeContainer;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.fractal.task.core.TaskMap.TaskHole;
import org.objectweb.fractal.task.deployment.lib.AbstractConfigurationTask;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;


/**
 * A {@link org.objectweb.fractal.adl.components.PrimitiveCompiler} to compile
 * {@link org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodes}
 * in definitions.
 *
 *
 * @author The ProActive Team
 *
 */
public class ExportedVirtualNodesCompiler implements PrimitiveCompiler, BindingController {
    private ExportedVirtualNodesBuilder builder;
    public final static String BUILDER_BINDING = "builder";

    public String[] listFc() {
        return new String[] { BUILDER_BINDING };
    }

    public Object lookupFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            return builder;
        }
        return null;
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.equals(BUILDER_BINDING)) {
            builder = (ExportedVirtualNodesBuilder) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            builder = null;
        }
    }

    public void compile(List<ComponentContainer> path, ComponentContainer container, TaskMap tasks,
            Map<Object, Object> context) throws ADLException {
        if (container instanceof ExportedVirtualNodesContainer) {
            ExportedVirtualNodes exported_vns = ((ExportedVirtualNodesContainer) container)
                    .getExportedVirtualNodes();
            if (exported_vns != null) {
                //                InstanceProviderTask c = (InstanceProviderTask) tasks.getTask("create",
                //                        container);
                String component_name = null;
                if (container instanceof Definition) {
                    component_name = ((Definition) container).getName();
                } else if (container instanceof Component) {
                    component_name = ((Component) container).getName();
                }

                //                else {
                //                    component_name = ((Definition) container).getName();
                //                }
                VirtualNode current_component_vn = (VirtualNode) ((VirtualNodeContainer) container)
                        .getVirtualNode();
                SetExportedVirtualNodesTask t = new SetExportedVirtualNodesTask(component_name, builder,
                    exported_vns.getExportedVirtualNodes(), current_component_vn);

                TaskMap.TaskHole virtualNodeTaskHole = tasks.addTask("exportedVirtualNodes", container, t);
                TaskHole createTask = tasks.getTaskHole("create", container);

                // exportations to be known *before* the creation of components.
                createTask.addDependency(virtualNodeTaskHole,
                        org.objectweb.fractal.task.core.Task.PREVIOUS_TASK_ROLE, context);
            }
        }
    }

    static class SetExportedVirtualNodesTask extends AbstractConfigurationTask {
        private ExportedVirtualNodesBuilder builder;
        private String componentName;
        private ExportedVirtualNode[] exported_vns;
        private VirtualNode currentComponentVN;

        public SetExportedVirtualNodesTask(String componentName, ExportedVirtualNodesBuilder builder,
                ExportedVirtualNode[] exported_vns, VirtualNode currentComponentVN) {
            this.componentName = componentName;
            this.builder = builder;
            this.exported_vns = exported_vns;
            this.currentComponentVN = currentComponentVN;
        }

        public Object getResult() {
            return null;
        }

        public void setPreviousTask(final TaskMap.TaskHole task) {
            addPreviousTask(task);
        }

        public void setResult(Object result) {
        }

        public void execute(Map<Object, Object> arg0) throws Exception {
            builder.compose(componentName, exported_vns, currentComponentVN);
        }
    }
}
