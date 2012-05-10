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
package org.objectweb.proactive.extensions.masterworker.core;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerMaster;


/**
 * A worker specialized in dealing with divisible tasks
 * (it will basically execute only one task and die)
 * @author The ProActive Team
 */
@ActiveObject
public class AODivisibleTaskWorker extends AOWorker implements RunActive, InitActive {

    /**
     * log4j logger of the worker
     */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER_WORKERS);
    private static final boolean debug = logger.isDebugEnabled();

    private SubMasterImpl submaster;
    private AOWorker parentWorker;
    private TaskIntern<Serializable> task;

    /**
     * ProActive no-arg constructor
     */
    @Deprecated
    public AODivisibleTaskWorker() {

    }

    public void initActivity(final Body body) {
        // Do nothing, overrides super class init activity
    }

    /**
     * Creates a worker with the given name
     * @param name name of the worker
     * @param provider the entity which will provide tasks to the worker
     * @param initialMemory initial memory of the worker
     */
    public AODivisibleTaskWorker(final String name, final WorkerMaster provider, final AOWorker parentWorker,
            final Map<String, Serializable> initialMemory, final TaskIntern<Serializable> task) {
        super(name, provider, initialMemory);
        this.parentWorker = parentWorker;
        this.submaster = new SubMasterImpl(provider, name, parentWorker);
        this.task = task;

    }

    public void readyToLive() {
        // do nothing it's just for synchronization
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        // Synchronization with the parent worker
        service.waitForRequest();
        service.serveOldest();

        // The single activity of this worker
        handleTask();

    }

    public void handleTask() {
        Serializable resultObj = null;
        boolean gotCancelled = false;
        ResultInternImpl result = new ResultInternImpl(task.getId());
        // We run the task and listen to exception thrown by the task itself
        try {
            if (debug) {
                logger.debug(name + " runs task " + task.getId() + "...");
            }

            //parentWorker.suspendWork();
            resultObj = task.run(memory, submaster);
            if (debug) {
                logger.debug(name + " task " + task.getId() + " is finished");
            }
        } catch (IsClearingError ex) {
            gotCancelled = true;

        } catch (MWFTError ex) {
            gotCancelled = true;

        } catch (Exception e) {
            result.setException(e);
        }
        if (!gotCancelled) {

            // We store the result inside our internal version of the task
            result.setResult(resultObj);
            if (debug) {
                logger
                        .debug(name + " sends the result of task " + result.getId() +
                            " and asks a new task...");
            }

            // We send the result back to the master

            BooleanWrapper wrap = provider.sendResult(result, name);
            // We synchronize the answer to avoid a BodyTerminatedException (the AO terminates right after this call)
            PAFuture.waitFor(wrap);
            // parentWorker.resumeWork();
        }
    }
}
