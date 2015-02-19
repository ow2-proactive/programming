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
package org.objectweb.proactive.core.body;

/**
 * This class is the default implementation of the Body interface.
 * An implementation of the Body interface, which lets the reified object
 * explicitly manage the queue of pending requests through its live() routine.
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since ProActive 0.9
 * @see org.objectweb.proactive.Body
 * @see AbstractBody
 */

import org.apache.log4j.Logger;
import org.objectweb.proactive.*;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ActiveBody extends BodyImpl implements Runnable, java.io.Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.BODY);

    //
    // -- STATIC MEMBERS -----------------------------------------------
    //
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    private transient InitActive initActive; // used only once when active object is started first time
    private RunActive runActive;
    private EndActive endActive;

    private boolean initActiveExecutionFailed = false;
    private Throwable lastErrorCaught = null;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Doesn't build anything, just for having one no-arg constructor
     */
    public ActiveBody() {
    }

    /**
     * Builds the body object, then fires its service thread
     */
    public ActiveBody(ConstructorCall c, String nodeURL, Active activity, MetaObjectFactory factory)
            throws java.lang.reflect.InvocationTargetException, ConstructorCallExecutionFailedException,
            ActiveObjectCreationException {
        // Creates the reified object
        super(c.execute(), nodeURL, factory);

        Object reifiedObject = this.localBodyStrategy.getReifiedObject();

        // InitActive
        if ((activity != null) && activity instanceof InitActive) {
            this.initActive = (InitActive) activity;
        } else if (reifiedObject instanceof InitActive) {
            this.initActive = (InitActive) reifiedObject;
        }

        // RunActive
        if ((activity != null) && activity instanceof RunActive) {
            this.runActive = (RunActive) activity;
        } else if (reifiedObject instanceof RunActive) {
            this.runActive = (RunActive) reifiedObject;
        } else {
            this.runActive = new FIFORunActive();
        }

        // EndActive
        if ((activity != null) && activity instanceof EndActive) {
            this.endActive = (EndActive) activity;
        } else if (reifiedObject instanceof EndActive) {
            this.endActive = (EndActive) reifiedObject;
        } else {
            this.endActive = null;
        }

        startBody();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements Runnable -----------------------------------------------
    //

    /**
     * The method executed by the active thread that will eventually launch the live
     * method of the active object of the default live method of this body.
     */
    public void run() {
        // Notify the rmi thread that the body has been registered into
        // the localBodyStore.
        synchronized (this) {
            activityStarted();
            this.notify();
        }

        boolean callTerminate = false;

        // run the activity of the body
        try {
            // execute the initialization if needed. Only once
            if (this.initActive != null) {
                try {
                    this.initActive.initActivity(this);
                } catch (Throwable t) {
                    initActiveExecutionFailed = true;
                    throw t;
                }

                this.initActive = null; // we won't do it again
            }

            /* We may race with a termination request in immediate service */
            RunActive thisRunActive = this.runActive;
            if (thisRunActive != null) {
                thisRunActive.runActivity(this);
            }

            // the body terminate its activity
            if (isActive()) {
                // serve remaining requests if non dead
                for (;;) {
                    BlockingRequestQueue queue;
                    try {
                        /* We may race with a termination request in immediate service */
                        queue = this.localBodyStrategy.getRequestQueue();
                        if (queue.isEmpty()) {
                            break;
                        }
                    } catch (ProActiveRuntimeException pre) {
                        break;
                    }

                    serve(queue.removeOldest());
                }
            }
        } catch (Throwable t) {
            lastErrorCaught = t;
            callTerminate = true;
            logger.error("Exception occurred in runActivity method of body " + toString() +
                ". Now terminating the body", t);
        } finally {
            // execute the end of activity
            if (this.endActive != null) {
                this.endActive.endActivity(this);
            }

            if (callTerminate) {
                terminate();
            } else if (isActive()) {
                activityStopped(!this.getFuturePool().remainingAC());
            }
        }
    }

    @Override
    public void terminate() {
        // Calls terminate for stopping the activity of the current active object
        // Once the method has been executed, we have the guarantee that
        // no more request can be served because the body strategy is replaced by
        // an InactiveLocalBodyStrategy
        super.terminate();

        // for fixing PALIGHT-73
        notifyPendingCallers(lastErrorCaught);
    }

    private void notifyPendingCallers(Throwable localErrorCatched) {
        if (initActiveExecutionFailed) {
            String errorMessage = "Exception thrown while executing InitActive on " + shortString();
            Exception exceptionSentToCaller = new ProActiveRuntimeException(errorMessage, localErrorCatched);
            final MethodCallResult methodCallResult = new MethodCallResult(null, exceptionSentToCaller);

            // At this step localBodyStrategy is an instance of InactiveLocalBodyStrategy
            // since terminate has been called just before
            Iterator<Request> it = ((InactiveLocalBodyStrategy) localBodyStrategy).getRemainingRequests()
                    .iterator();

            ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime()
                    .availableProcessors());

            // Notifies the callers of the method calls received that an issue has occurred with initActive
            // It is used to prevent callers to wait forever for a future or an acknowledgement
            while (it.hasNext()) {
                final Request request = it.next();

                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!request.getMethodCall().isOneWayCall()) {
                            try {
                                request.getSender().receiveReply(
                                        new ReplyImpl(getID(), request.getSequenceNumber(), request
                                                .getMethodName(), methodCallResult));
                            } catch (IOException e) {
                                sendReplyExceptionsLogger.error(shortString() +
                                    " : Failed to send reply to method:" + request.getMethodName() +
                                    " sequence: " + request.getSequenceNumber() + " by " +
                                    request.getSenderNodeURL() + "/" + request.getSender(), e);
                            }
                        }
                    }
                });
            }
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Creates the active thread and start it using this runnable body.
     */
    public void startBody() {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Body");
        }

        Thread t = new Thread(this, shortClassName(getName()) + " on " + getNodeURL());

        // Wait for the registration of this Body inside the LocalBodyStore
        // to avoid a race condition (t not yet scheduled and getActiveObjects() called)
        synchronized (this) {
            t.start();
            try {
                this.wait();
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * Signals that the activity of this body, managed by the active thread has just stopped.
     *
     * @param completeACs if true, and if there are remaining AC in the futurepool, the AC thread
     *                    is not killed now; it will be killed after the sending of the last remaining AC.
     */
    @Override
    protected void activityStopped(boolean completeACs) {
        super.activityStopped(completeACs);
        this.runActive = null;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private static String shortClassName(String fqn) {
        int n = fqn.lastIndexOf('.');
        if ((n == -1) || (n == (fqn.length() - 1))) {
            return fqn;
        }
        return fqn.substring(n + 1);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("out = " + out);
        }
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("in = " + in);
        }
        in.defaultReadObject();
        startBody();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    private class FIFORunActive implements RunActive, java.io.Serializable {
        public void runActivity(Body body) {
            new Service(body).fifoServing();
        }
    }

    /*
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getNextSequenceID()
     */
    @Override
    public long getNextSequenceID() {
        return localBodyStrategy.getNextSequenceID();
    }

}
