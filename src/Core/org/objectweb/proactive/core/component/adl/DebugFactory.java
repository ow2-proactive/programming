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
package org.objectweb.proactive.core.component.adl;

import java.util.Map;

import org.objectweb.fractal.adl.ADLErrors;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.BasicFactory;
import org.objectweb.fractal.adl.Compiler;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.FractalADLTaskMap;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.task.core.Scheduler;
import org.objectweb.fractal.task.core.Task;
import org.objectweb.fractal.task.core.TaskExecutionException;
import org.objectweb.fractal.task.core.TaskMap;


/**
 * Debug version of the {@link BasicFactory}, provided by Fractal. 
 * This class does the same as {@link BasicFactory}, but include some debug messages
 * to visualize the result of each phase, and to avoid modifying directly the Fractal factory.
 * After the extension of the GCM ADL Factory, it can be safely replaced back by the {@link BasicFactory}.
 * 
 * As the {@link BasicFactory}, it uses a {@link Loader} to load ADL definitions, 
 * a {@link Compiler} to compile and a {@link Scheduler} to execute the compiled tasks.
 * 
 * @author The ProActive Team
 */
public class DebugFactory implements BindingController, Factory {

    /**
     * Name of the client interface bound to the {@link Loader} used by this
     * factory.
     */
    public static final String LOADER_BINDING = "loader";

    /**
     * Name of the client interface bound to the {@link Compiler} used by this
     * factory.
     */
    public static final String COMPILER_BINDING = "compiler";

    /**
     * Name of the client interface bound to the {@link Scheduler} used by this
     * factory.
     */
    public static final String SCHEDULER_BINDING = "scheduler";

    /**
     * The {@link Loader} used by this factory.
     */
    // TODO rename loader to loaderItf
    public Loader loader;

    /**
     * The {@link Compiler} used by this factory.
     */
    // TODO rename compiler to compilerItf
    public Compiler compiler;

    /**
     * The {@link Scheduler} used by this factory.
     */
    // TODO rename scheduler to schedulerItf
    public Scheduler scheduler;

    // --------------------------------------------------------------------------
    // Implementation of the BindingController interface
    // --------------------------------------------------------------------------

    public String[] listFc() {
        return new String[] { LOADER_BINDING, COMPILER_BINDING, SCHEDULER_BINDING };
    }

    public Object lookupFc(final String itf) {
        if (itf.equals(LOADER_BINDING)) {
            return loader;
        } else if (itf.equals(COMPILER_BINDING)) {
            return compiler;
        } else if (itf.equals(SCHEDULER_BINDING)) {
            return scheduler;
        }
        return null;
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.equals(LOADER_BINDING)) {
            loader = (Loader) value;
        } else if (itf.equals(COMPILER_BINDING)) {
            compiler = (Compiler) value;
        } else if (itf.equals(SCHEDULER_BINDING)) {
            scheduler = (Scheduler) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals(LOADER_BINDING)) {
            loader = null;
        } else if (itf.equals(COMPILER_BINDING)) {
            compiler = null;
        } else if (itf.equals(SCHEDULER_BINDING)) {
            scheduler = null;
        }
    }

    // --------------------------------------------------------------------------
    // Implementation of the Factory interface
    // --------------------------------------------------------------------------

    // Suppress unchecked warning to avoid to change Factory interface
    @SuppressWarnings("unchecked")
    public Object newComponentType(final String name, final Map context) throws ADLException {
        final Definition d = loader.load(name, context);
        final TaskMap m = new FractalADLTaskMap();
        compiler.compile(d, m, context);
        try {
            m.getTask("type", d).execute(context);

            // return ((FactoryProviderTask)m.getTask("type", d)).getFactory();
            /* XXX this is a more generic way to obtain the Task result */
            return (m.getTask("type", d)).getResult();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Suppress unchecked warning to avoid to change Factory interface
    @SuppressWarnings("unchecked")
    public Object newComponent(final String name, final Map context) throws ADLException {
        final Definition d = loader.load(name, context);

        final TaskMap m = new FractalADLTaskMap();
        compiler.compile(d, m, context);
        final Task[] tasks = m.getTasks();

        try {
            System.err.println();
            System.err.println("---------------------------------------------------------------------");
            System.err.println("[Scheduler] Task execution:");
            scheduler.schedule(tasks, context);
        } catch (final TaskExecutionException tee) {
            tee.printStackTrace();
            throw new ADLException(ADLErrors.TASK_EXECUTION_ERROR, tee);
        }

        // return ((InstanceProviderTask)m.getTask("create", d)).getInstance();
        /* XXX this is a more generic way to obtain the Task result */
        return (m.getTask("create", d)).getResult();
    }
}
