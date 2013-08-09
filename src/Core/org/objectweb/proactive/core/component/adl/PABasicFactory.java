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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.objectweb.fractal.adl.ADLErrors;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.BasicFactory;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.FractalADLTaskMap;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.task.core.Scheduler;
import org.objectweb.fractal.task.core.Task;
import org.objectweb.fractal.task.core.TaskExecutionException;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.proactive.utils.NamedThreadFactory;


/**
 * ProActive/GCM Basic implementation of the {@link Factory} interface.
 * <br>
 * This implementation uses a {@link Loader} to load ADL definitions, a {@link Compiler}
 * to compile them, and a {@link Scheduler} to execute the compiled tasks.
 * 
 * @author The ProActive Team
 */
public class PABasicFactory extends BasicFactory implements PAFactory {
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings( { "rawtypes", "unchecked" })
    public Object[] newComponentsInParallel(String name, final Map context, int nbComponents)
            throws ADLException {
        final Definition definition = loader.load(name, context);
        ThreadFactory threadFactory = new NamedThreadFactory("PABasicFactory Thread Factory");
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors(), threadFactory);
        List<Future<Object>> futures = new ArrayList<Future<Object>>();

        for (int i = 0; i < nbComponents; i++) {
            futures.add(executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    final TaskMap taskMap = new FractalADLTaskMap();
                    compiler.compile(definition, taskMap, context);
                    final Task[] tasks = taskMap.getTasks();

                    try {
                        scheduler.schedule(tasks, context);
                    } catch (final TaskExecutionException tee) {
                        tee.printStackTrace();
                        throw new ADLException(ADLErrors.TASK_EXECUTION_ERROR, tee);
                    }
                    return (taskMap.getTask("create", definition)).getResult();
                }
            }));
        }

        Object[] components = new Object[nbComponents];

        for (int i = 0; i < futures.size(); i++) {
            try {
                components[i] = futures.get(i).get();
            } catch (InterruptedException e) {
                throw new ADLException(PAADLErrors.EXECUTOR_ERROR, e.getMessage());
            } catch (ExecutionException e) {
                throw new ADLException(PAADLErrors.EXECUTOR_ERROR, e.getMessage());
            }
        }

        return components;
    }
}
