/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.masterworker.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.Cache;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.masterworker.interfaces.DivisibleTask;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.Worker;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerMaster;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The Workers Active Objects are the workers in the Master/Worker API.<br>
 * They execute tasks needed by the master
 *
 * @author The ProActive Team
 */
@ActiveObject
public class AOWorker implements InitActive, Serializable, Worker {

    /** log4j logger of the worker */
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER_WORKERS);
    protected static final boolean debug = logger.isDebugEnabled();

    protected boolean suspended = false;
    protected boolean wakingup = false;

    /** stub on this active object */
    protected AOWorker stubOnThis;

    /** Name of the worker */
    protected String name;

    /** The entity which will provide tasks to the worker (i.e. the master) */
    protected WorkerMaster provider;

    protected Map<String, Serializable> initialMemory;
    protected transient WorkerMemory memory;

    /** The current list of tasks to compute */
    protected Queue<TaskIntern<Serializable>> pendingTasks;
    protected Queue<Queue<TaskIntern<Serializable>>> pendingTasksFutures;

    private long subWorkerNameCounter = 0;

    /** ProActive no arg contructor */
    public AOWorker() {
    }

    /**
     * Creates a worker with the given name
     *
     * @param name          name of the worker
     * @param provider      the entity which will provide tasks to the worker
     * @param initialMemory initial memory of the worker
     */
    public AOWorker(final String name, final WorkerMaster provider,
            final Map<String, Serializable> initialMemory) {
        this.name = name;
        this.provider = provider;
        this.memory = new WorkerMemoryImpl(initialMemory);
        this.initialMemory = initialMemory;
        this.pendingTasksFutures = new LinkedList<Queue<TaskIntern<Serializable>>>();
        this.pendingTasks = new LinkedList<TaskIntern<Serializable>>();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof AOWorker) && name.equals(((Worker) obj).getName());
    }

    /** {@inheritDoc} */
    @Cache
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /** {@inheritDoc} */
    public BooleanWrapper heartBeat() {
        // we want the rendez-vous to work
        // But we need as well an answer for synchronization
        return new BooleanWrapper(true);
    }

    /** {@inheritDoc} */
    public void initActivity(final Body body) {
        stubOnThis = (AOWorker) PAActiveObject.getStubOnThis();
        PAActiveObject.setImmediateService("heartBeat");
        //PAActiveObject.setImmediateService("terminate");

        // Initial Task
        stubOnThis.getTaskAndSchedule();
    }

    /** gets the initial task to solve */
    protected void getTasks() {
        if (debug) {
            logger.debug(name + " asks a new task...");
        }
        Queue<TaskIntern<Serializable>> newTasks;
        if ((pendingTasks.size() == 0) && (pendingTasksFutures.size() == 0)) {
            if (debug) {
                logger.debug(name + " requests a task flooding...");
            }
            newTasks = provider.getTasks(stubOnThis, name, true);
        } else {
            newTasks = provider.getTasks(stubOnThis, name, false);
        }
        pendingTasksFutures.offer(newTasks);

    }

    /** gets the initial task to solve */
    protected void getTasksWithResult(ResultInternImpl result) {
        if (debug) {
            logger.debug(name + " sends the result of task " + result.getId() + " and asks a new task...");
        }
        Queue<TaskIntern<Serializable>> newTasks;
        if ((pendingTasks.size() == 0) && (pendingTasksFutures.size() == 0)) {
            if (debug) {
                logger.debug(name + " requests a task flooding...");
            }
            newTasks = provider.sendResultAndGetTasks(result, name, true);
        } else {

            newTasks = provider.sendResultAndGetTasks(result, name, false);
        }
        pendingTasksFutures.offer(newTasks);

    }

    public void suspendWork() {
        if (debug) {
            logger.debug(name + " suspended");
        }

        suspended = true;
    }

    public void resumeWork() {
        if (debug) {
            logger.debug(name + " resumed work");
        }
        if (suspended) {
            suspended = false;
            stubOnThis.scheduleTask();
        }
    }

    /** gets the initial task to solve */
    public void getTaskAndSchedule() {
        // We get some tasks
        getTasks();

        // We schedule the execution
        stubOnThis.scheduleTask();
    }

    /**
     * Handle a task (run it)
     *
     * @param task task to run
     */
    public void handleTask(final TaskIntern<Serializable> task) {
        wakingup = false;

        // if the task is a divisible one, we spawn a new specialized worker for it
        if (task.getTask() instanceof DivisibleTask) {
            String newWorkerName = name + "_" + subWorkerNameCounter;
            subWorkerNameCounter = (subWorkerNameCounter + 1) % (Long.MAX_VALUE - 1);
            AODivisibleTaskWorker spawnedWorker = null;
            try {
                spawnedWorker = (AODivisibleTaskWorker) PAActiveObject.newActive(AODivisibleTaskWorker.class
                        .getName(),
                        new Object[] { newWorkerName, provider, stubOnThis, initialMemory, task },
                        PAActiveObject.getNode());

            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (NodeException e) {
                e.printStackTrace();
            }
            // We tell the master that we forwarded the task to another worker
            PAFuture.waitFor(provider.forwardedTask(task.getId(), name, newWorkerName));
            // We tell the worker that it's now known by the master and can do it's job
            spawnedWorker.readyToLive();
            // We get some new tasks
            getTasks();
            // // but we are suspended
            // suspendWork();
        } else {
            Serializable resultObj = null;
            ResultInternImpl result = new ResultInternImpl(task.getId());

            // We run the task and listen to exception thrown by the task itself
            try {
                if (debug) {
                    logger.debug(name + " runs task " + task.getId() + "...");
                }

                resultObj = task.run(memory);

            } catch (Exception e) {
                result.setException(e);
            }

            // We store the result inside our internal version of the task
            result.setResult(resultObj);
            // We send the result back and ask for new tasks
            getTasksWithResult(result);
        }
        // Schedule
        stubOnThis.scheduleTask();
    }

    /** ScheduleTask : find a new task to run */
    public void scheduleTask() {
        while ((pendingTasks.size() == 0) && (pendingTasksFutures.size() > 0)) {
            pendingTasks.addAll(pendingTasksFutures.remove());
        }

        if (!suspended && (pendingTasks.size() > 0)) {

            TaskIntern<Serializable> newTask = pendingTasks.remove();
            // We handle the current Task
            stubOnThis.handleTask(newTask);

        } else {
            // if there is nothing to do or if we are suspended we sleep
            wakingup = false;
        }
    }

    /** {@inheritDoc} */
    public BooleanWrapper terminate() {
        if (debug) {
            logger.debug("Terminating " + name + "...");
        }
        provider = null;
        stubOnThis = null;
        ((WorkerMemoryImpl) memory).clear();
        initialMemory.clear();

        PAActiveObject.terminateActiveObject(false);
        if (debug) {
            logger.debug(name + " terminated...");
        }

        return new BooleanWrapper(true);
    }

    /** {@inheritDoc} */
    public void wakeup() {
        if (debug) {
            logger.debug(name + " receives a wake up message...");
        }

        if (pendingTasks.size() > 0 || suspended || wakingup) {
            if (debug) {
                logger.debug(name + " ignored wake up message ...");
            }
        } else {
            if (debug) {
                logger.debug(name + " wakes up...");
            }
            wakingup = true;
            // Initial Task
            stubOnThis.getTaskAndSchedule();
        }

    }

    public void clear() {
        pendingTasks.clear();
        pendingTasksFutures.clear();
        Service service = new Service(PAActiveObject.getBodyOnThis());
        service.flushAll();
        provider.isCleared(stubOnThis);
    }

}
