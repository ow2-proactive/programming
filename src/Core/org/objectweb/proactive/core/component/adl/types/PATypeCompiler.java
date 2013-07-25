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
package org.objectweb.proactive.core.component.adl.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.attributes.Attributes;
import org.objectweb.fractal.adl.attributes.AttributesContainer;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeCompiler;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.fractal.task.deployment.lib.AbstractFactoryProviderTask;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.control.PAInterceptorController;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The {@link PATypeCompiler} compiles the &lt;interface&gt; nodes, 
 * and creates the {@link InterfaceType} and {@link ComponentType} objects
 * that must exist prior to creating a component.<br/><br/>
 * 
 * The creation task is performed by a {@link CreateTypeTask} that collects the data about the F
 * and NF interfaces of a component (arrays of {@link InterfaceType} objects), and uses them to create
 * a {@link ComponentType}, which is the outcome of the execution.<br/><br/>
 * 
 * The actual creation tasks, using the GCM API, are delegated to a {@link PATypeBuilder}.
 * 
 * 
 * @author The ProActive Team
 *
 */
public class PATypeCompiler extends TypeCompiler {

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    @Override
    public void compile(List path, ComponentContainer container, TaskMap tasks, Map context)
            throws ADLException {

        logger.debug("[PATypeCompiler] Compiling container " + container.toString());

        if (container instanceof InterfaceContainer) {
            try {
                // the task may already exist, in case of a shared component
                tasks.getTask("type", container);
            } catch (NoSuchElementException e) {
                CreateTypeTask createTypeTask = new CreateTypeTask((PATypeBuilderItf) builder,
                    (InterfaceContainer) container);
                tasks.addTask("type", container, createTypeTask);
            }
        }

    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    /**
     * 
     * The {@link CreateTypeTask} collects the data from the InterfaceType and delegates the 
     * type creation to a {@link PATypeBuilderItf}.
     * 
     * @author The ProActive Team
     *
     */
    static class CreateTypeTask extends AbstractFactoryProviderTask {

        private PATypeBuilderItf builder;

        private InterfaceContainer container;

        public CreateTypeTask(final PATypeBuilderItf builder, final InterfaceContainer container) {
            this.builder = builder;
            this.container = container;
        }

        public void execute(final Map<Object, Object> context) throws Exception {
            if (getFactory() != null) {
                return;
            }

            String name = null;
            if (container instanceof Definition) {
                name = ((Definition) container).getName();
            } else if (container instanceof Component) {
                name = ((Component) container).getName();
            }
            logger.debug("[PATypeCompiler] Executing CreateTypeTask for " +
                (container.astGetDecoration("NF") == null ? " F" : "NF") + " component " + name);

            // collects and creates the F interfaces
            List<Object> fItfTypes = new ArrayList<Object>();
            Interface[] fItfs = container.getInterfaces();
            boolean hasInterceptors = false;
            for (Interface itf : fItfs) {
                logger.debug("[PATypeCompiler] --> ITF: " + itf.toString());
                if (itf instanceof TypeInterface) {
                    PATypeInterface tItf = (PATypeInterface) itf;
                    Object itfType = builder.createInterfaceType(tItf.getName(), tItf.getSignature(), tItf
                            .getRole(), tItf.getContingency(), tItf.getCardinality(), context);
                    fItfTypes.add(itfType);

                    if (tItf.getInterceptors() != null) {
                        hasInterceptors = true;
                    }
                }
            }

            // collects and creates the NF interfaces
            List<Object> nfItfTypes = new ArrayList<Object>();
            Interface[] nfItfs = null;
            boolean membraneControllerDefined = false;
            boolean interceptorControllerDefined = false;
            if (container instanceof ControllerContainer) {
                Controller ctrl = ((ControllerContainer) container).getController();
                if (ctrl != null) {
                    if (ctrl instanceof InterfaceContainer) {
                        nfItfs = ((InterfaceContainer) ctrl).getInterfaces();
                        for (Interface itf : nfItfs) {
                            //logger.debug("[PATypeCompiler] --> ITF: " + itf.toString() );
                            if (itf instanceof TypeInterface) {
                                TypeInterface tItf = (TypeInterface) itf;
                                logger.debug("[PATypeCompiler] --> ITF: " + itf.toString() + " role: " +
                                    tItf.getRole());
                                boolean isInternal = PATypeInterface.INTERNAL_CLIENT_ROLE.equals(tItf
                                        .getRole()) ||
                                    PATypeInterface.INTERNAL_SERVER_ROLE.equals(tItf.getRole());
                                Object itfType = builder.createInterfaceType(tItf.getName(), tItf
                                        .getSignature(), tItf.getRole(), tItf.getContingency(), tItf
                                        .getCardinality(), isInternal, context);
                                nfItfTypes.add(itfType);
                                if (Constants.MEMBRANE_CONTROLLER.equals(tItf.getName())) {
                                    membraneControllerDefined = true;
                                } else if (Constants.INTERCEPTOR_CONTROLLER.equals(tItf.getName())) {
                                    interceptorControllerDefined = true;
                                }
                            }
                        }
                        // if there are interfaces described in the membrane, and none of them is the "membrane-controller", then add the interface
                        if (nfItfs.length > 0 && !membraneControllerDefined) {
                            Object itfType = builder.createInterfaceType(Constants.MEMBRANE_CONTROLLER,
                                    PAMembraneController.class.getName(), TypeInterface.SERVER_ROLE,
                                    TypeInterface.MANDATORY_CONTINGENCY, TypeInterface.SINGLETON_CARDINALITY,
                                    context);
                            nfItfTypes.add(itfType);
                        }

                        if (hasInterceptors && !interceptorControllerDefined) {
                            Object itfType = builder.createInterfaceType(Constants.INTERCEPTOR_CONTROLLER,
                                    PAInterceptorController.class.getName(), TypeInterface.SERVER_ROLE,
                                    TypeInterface.MANDATORY_CONTINGENCY, TypeInterface.SINGLETON_CARDINALITY,
                                    context);
                            nfItfTypes.add(itfType);
                        }
                    }
                }
            }

            /* TODO improve module separation (how?) */
            if (container instanceof AttributesContainer) {
                Attributes attr = ((AttributesContainer) container).getAttributes();
                if (attr != null) {
                    Object itfType = builder.createInterfaceType(Constants.ATTRIBUTE_CONTROLLER, attr
                            .getSignature(), TypeInterface.SERVER_ROLE, TypeInterface.MANDATORY_CONTINGENCY,
                            TypeInterface.SINGLETON_CARDINALITY, context);
                    fItfTypes.add(itfType);
                }
            }

            setFactory(builder.createComponentType(name, fItfTypes.toArray(), nfItfTypes.toArray(), context));
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[CreateTypeTask()]";
        }

    }
}
