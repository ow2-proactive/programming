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
package org.objectweb.proactive.core.component.adl.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Compiler;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The PAComponentCompiler DOES NOT extend ComponentCompiler, because it seems that that one is not thought
 * to be extendible (private fields and classes). So this one, implements directly Compiler.
 * 
 * 
 * @author The ProActive Team
 *
 */
public class PAComponentCompiler implements BindingController, Compiler {

    /**
     * Name of the collection interface bound to the {@link PrimitiveCompiler}s
     * used by this compiler.
     */
    public static final String PRIMITIVE_COMPILERS_BINDING = "primitive-compilers";

    /**
     * The primitive compilers used by this compiler.
     */
    private final Map<String, PrimitiveCompiler> primitiveCompilers = new TreeMap<String, PrimitiveCompiler>();

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    // --------------------------------------------------------------------------
    // Implementation of the BindingController interface
    // --------------------------------------------------------------------------

    public String[] listFc() {
        return primitiveCompilers.keySet().toArray(new String[primitiveCompilers.size()]);
    }

    public Object lookupFc(final String itf) {
        if (itf.startsWith(PRIMITIVE_COMPILERS_BINDING)) {
            return primitiveCompilers.get(itf);
        } else {
            return null;
        }
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.startsWith(PRIMITIVE_COMPILERS_BINDING)) {
            primitiveCompilers.put(itf, (PrimitiveCompiler) value);
        }
    }

    public void unbindFc(final String itf) {
        if (itf.startsWith(PRIMITIVE_COMPILERS_BINDING)) {
            primitiveCompilers.remove(itf);
        }
    }

    // --------------------------------------------------------------------------
    // Implementation of the Compiler interface
    // --------------------------------------------------------------------------

    public void compile(final Definition definition, final TaskMap tasks, final Map<Object, Object> context)
            throws ADLException {
        if (definition instanceof ComponentContainer) {
            compile(new ArrayList<ComponentContainer>(), (ComponentContainer) definition, tasks, context);
        }
    }

    // --------------------------------------------------------------------------
    // Compilation methods
    // --------------------------------------------------------------------------

    void compile(final List<ComponentContainer> path, final ComponentContainer container,
            final TaskMap tasks, final Map<Object, Object> context) throws ADLException {

        if (container.astGetDecoration("NF") == null) {
            logger.debug("[PAComponentCompiler] Compiling  F component " + container.toString());
        } else {
            logger.debug("[PAComponentCompiler] Compiling NF component " + container.toString());
        }

        path.add(container);
        final Component[] comps = getAllComponents(container);
        for (final Component element : comps) {
            compile(path, element, tasks, context);
        }
        path.remove(path.size() - 1);

        final Iterator<PrimitiveCompiler> it = primitiveCompilers.values().iterator();
        while (it.hasNext()) {
            (it.next()).compile(path, container, tasks, context);
        }
    }

    /**
     * Helper method to collect all inner components, including F subcomponents,
     * and NF components in the membrane.
     * 
     * @param container
     * @return
     */
    private Component[] getAllComponents(ComponentContainer container) {

        Component[] fComps = container.getComponents();
        Component[] nfComps = null;

        if (container instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) container).getController();
            if (ctrl != null) {
                if (ctrl instanceof ComponentContainer) {
                    nfComps = ((ComponentContainer) ctrl).getComponents();
                }
            }
        }

        Component[] comps = new Component[fComps.length + (nfComps == null ? 0 : nfComps.length)];
        System.arraycopy(fComps, 0, comps, 0, fComps.length);
        if (nfComps != null) {
            System.arraycopy(nfComps, 0, comps, fComps.length, nfComps.length);
        }

        return comps;
    }
}
