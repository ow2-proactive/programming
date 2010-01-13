/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.debugger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ObjectForSynchro;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.debug.tools.ArrayListSynchronized;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class DebuggerImpl implements Debugger {

    private static final long serialVersionUID = 420L;

    private static Logger logger = ProActiveLogger.getLogger(Loggers.DEBUGGER);

    /** internal use only : for wait/notify */
    private ObjectForSynchro objectForBlocks = new ObjectForSynchro();

    /** Step by step mode enabled/disabled */
    private boolean stepByStepMode;

    /** the ActivableObject state */
    private DebugInfo debugInfo;

    /** Time of breaks, in milliseconds */
    private long slowMotionDelay = 0;

    /** List of blocked breakpoints */
    private Map<Long, BreakpointInfo> breakpoints = new HashMap<Long, BreakpointInfo>();

    /** internal use only : keep all breakpoints blocked */
    private boolean stayBlocked = false;

    /** Type of breakpoint present in filter */
    private Set<BreakpointType> breakpointTypeFilter = new HashSet<BreakpointType>();

    private AbstractBody body;

    // ----- ExtendedDebugger attributes
    private boolean extendedDebugger = false;
    private UniversalBody destinationBody;
    private ObjectForSynchro lockForConnection = new ObjectForSynchro();
    private boolean notificationReceived = false;
    @SuppressWarnings("unused")
    private String cachedCanonStringBodyID = ""; // to get this info in the eclipse debugger
    private Request currentRequest = null;
    private ArrayListSynchronized<Request> requestQueue = new ArrayListSynchronized<Request>();

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public DebuggerImpl() {
        initBreakpointTypes();
        setStepByStep(PAProperties.PA_DEBUG.isTrue());
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    protected void block(BreakpointInfo info) {
        try {
            do {
                synchronized (objectForBlocks) {
                    objectForBlocks.wait(slowMotionDelay);
                }
            } while (!canBeResumed(info));
        } catch (InterruptedException e) {
            logger.warn(e);
        }
    }

    protected boolean canBeResumed(BreakpointInfo info) {
        return !stayBlocked && isSlowMotionEnabled() || !breakpoints.containsKey(info.getBreakpointId());
    }

    protected boolean canBeBlocked(BreakpointType type) {
        return isStepByStepMode() && breakpointTypeFilter.contains(type);
    }

    protected void unblock() {
        synchronized (objectForBlocks) {
            objectForBlocks.notifyAll();
        }
    }

    protected void addInfo(BreakpointInfo info) {
        breakpoints.put(info.getBreakpointId(), info);
        updateInfo();
    }

    protected void removeInfo(BreakpointInfo info) {
        breakpoints.remove(info.getBreakpointId());
        updateInfo();
    }

    protected void updateInfo() {
        if (debugInfo != null) {
            debugInfo.setInfo(this);
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#initBreakpointTypes()
     */
    public void initBreakpointTypes() {
        disableAllBreakpointTypes();
        for (BreakpointType type : BreakpointType.values()) {
            if (!type.equals(BreakpointType.SendRequest))
                breakpointTypeFilter.add(type);
        }
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#updateBreakpointTypes(Map<String, Boolean> types)
     */
    public void updateBreakpointTypes(Map<String, Boolean> values) {
        for (BreakpointType type : BreakpointType.values()) {
            if (values.get(type.name())) {
                breakpointTypeFilter.add(type);
            } else {
                breakpointTypeFilter.remove(type);
            }
        }
        updateInfo();
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#removeBreakpointTypes(BreakpointType[])
     */
    public void disableAllBreakpointTypes() {
        breakpointTypeFilter.clear();
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#blockForConnection()
     */
    public void breakpoint(BreakpointType type, UniversalBody destinationBody) { // SendRequest
        breakpoint(type, new RequestImpl()); // for the StepByStep
        // ------------ ExtendedDebugger
        if (extendedDebugger) {
            this.destinationBody = destinationBody;
            new Thread(new Runnable() {
                public void run() {
                    while (!notificationReceived) { // if the notification is lost
                        sendNotification(NotificationType.connectDebugger);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            try {
                synchronized (lockForConnection) {
                    if (!notificationReceived)
                        lockForConnection.wait();
                    notificationReceived = false;
                }
            } catch (InterruptedException e) {
                logger.warn(e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#breakpoint(BreakpointType)
     */
    public void breakpoint(BreakpointType type, Request request) {
        if (type.equals(BreakpointType.NewService)) {
            currentRequest = request;
        }

        // ------------ StepByStep
        if (canBeBlocked(type)) {
            BreakpointInfo info = new BreakpointInfo(type, Thread.currentThread(), request);
            boolean hasAlreadyIS = debugInfo.hasImmediate();
            addInfo(info);
            if (logger.isDebugEnabled()) {
                logger.debug("Breakpoint #" + info.getBreakpointId() + " (" + type + ") : suspend execution");
            }
            sendNotification(NotificationType.stepByStepBlocked);
            if (!hasAlreadyIS && type.isImmediate()) {
                sendNotification(NotificationType.stepByStepISEnabled);
            }
            block(info);
            sendNotification(NotificationType.stepByStepResumed);
            if (logger.isDebugEnabled()) {
                logger.debug("Breakpoint #" + info.getBreakpointId() + " (" + type + ") : resume execution");
            }
            removeInfo(info);
            if (!debugInfo.hasImmediate()) {
                sendNotification(NotificationType.stepByStepISDisabled);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#resume()
     */
    public void resume() {
        setStepByStep(false);
        breakpoints.clear();
        unblock();
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#nextStep()
     */
    public void nextStep() {
        breakpoints.clear();
        unblock();
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#nextStep(long)
     */
    public void nextStep(long id) {
        breakpoints.remove(id);
        unblock();
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#nextStep(Collection)
     */
    public void nextStep(Collection<Long> ids) {
        breakpoints.remove(ids);
        unblock();
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#slowMotion(long)
     */
    public void slowMotion(long slowMotionDelay) {
        slowMotion(slowMotionDelay, true);
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#slowMotion(long,
     *      boolean)
     */
    public void slowMotion(long slowMotionDelay, boolean useImmediatly) {
        this.slowMotionDelay = slowMotionDelay;
        if (isStepByStepMode()) {
            if (slowMotionDelay == 0) {
                sendNotification(NotificationType.stepByStepSlowMotionDisabled);
            } else {
                sendNotification(NotificationType.stepByStepSlowMotionEnabled);
            }
        }
        if (useImmediatly && isBlockedInBreakpoint()) {
            stayBlocked = true;
            unblock();
            stayBlocked = false;
        }
    }

    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#disableSlowMotion()
     */
    public void disableSlowMotion() {
        this.slowMotionDelay = 0;
    }

    public boolean isBlockedInBreakpoint() {
        return !breakpoints.isEmpty();
    }

    public void sendNotification(String type) {
        if (body != null) {
            BodyWrapperMBean mbean = body.getMBean();
            if (mbean != null) {
                if (type.equals(NotificationType.connectDebugger)) {
                    try {
                        mbean.sendNotification(type, NodeFactory.getNode(destinationBody.getNodeURL())
                                .getProActiveRuntime().getURL());
                    } catch (NodeException e) {
                        e.printStackTrace();
                    }
                } else {
                    mbean.sendNotification(type);
                }
            }
        }
    }

    //
    // -- EXTENDED DEBUGGER ------------------------------------------------
    //
    /**
     * @see org.objectweb.proactive.core.debug.debugger.Debugger#unblockConnection()
     */
    public void unblockConnection() {
        synchronized (lockForConnection) {
            notificationReceived = true;
            lockForConnection.notifyAll();
        }
    }

    public void enableExtendedDebugger() {
        this.extendedDebugger = true;
    }

    public void disableExtendedDebugger() {
        this.extendedDebugger = false;
    }

    public RequestQueueInfo getRequestQueueInfo() {
        BlockingRequestQueue brq = body.getRequestQueue();
        Iterator<Request> i = brq.iterator();
        requestQueue.clear();
        while (i.hasNext()) {
            requestQueue.add(i.next());
        }
        return new RequestQueueInfo(requestQueue, currentRequest);
    }

    /**
     *
     * @param sequenceNumber
     */
    private synchronized void moveRequest(final long sequenceNumber, int way) {
        BlockingRequestQueue brq = body.getRequestQueue();
        Iterator<Request> i = brq.iterator();

        // find the Request corresponding to the sequence number
        // and Save the RequestQueue
        Request toMove = null;
        List<Request> requestList = new ArrayList<Request>();
        int position = 0;
        while (i.hasNext()) {
            Request r = i.next();
            requestList.add(r);
            if (r.getSequenceNumber() == sequenceNumber) {
                toMove = r;
            }
            if (toMove == null)
                position++;
        }

        // Move the request
        if (toMove != null &&
            ((position > 0 && way == -1) || (position < requestList.size() - 1 && way == 1))) {
            position += way;
            brq.clear();
            int invertIndice = (way == 1 ? position + 1 : position);
            for (int j = 0; j < invertIndice; j++) {
                if (requestList.get(j) != toMove)
                    brq.add(requestList.get(j));
            }
            brq.add(toMove);
            for (int j = invertIndice; j < requestList.size(); j++) {
                if (requestList.get(j) != toMove)
                    brq.add(requestList.get(j));
            }
            sendNotification(NotificationType.requestQueueModified);
        }
    }

    public void moveUpRequest(final long sequenceNumber) {
        moveRequest(sequenceNumber, -1);
    }

    public void moveDownRequest(final long sequenceNumber) {
        moveRequest(sequenceNumber, 1);
    }

    //
    // -- GETTERS AND SETTERS -----------------------------------------------
    //
    public boolean isStepByStepMode() {
        return stepByStepMode;
    }

    public boolean isSlowMotionEnabled() {
        return getSlowMotionDelay() != 0;
    }

    public Set<BreakpointType> getBreakpointTypeFilter() {
        return breakpointTypeFilter;
    }

    public Map<Long, BreakpointInfo> getBreakpoints() {
        return breakpoints;
    }

    public DebugInfo getDebugInfo() {
        return debugInfo;
    }

    public long getSlowMotionDelay() {
        return slowMotionDelay;
    }

    public void setTarget(AbstractBody target) {
        this.debugInfo = new DebugInfo(target);
        body = target;
        cachedCanonStringBodyID = body.getID().getCanonString();
        if (!(target instanceof BodyImpl)) { // half body
            disableAllBreakpointTypes();
        }
        updateInfo();
    }

    public void setStepByStep(boolean stepByStep) {
        this.stepByStepMode = stepByStep;
        if (stepByStep) {
            sendNotification(NotificationType.stepByStepEnabled);
            if (isSlowMotionEnabled()) {
                sendNotification(NotificationType.stepByStepSlowMotionEnabled);
            }
        } else {
            if (isSlowMotionEnabled()) {
                sendNotification(NotificationType.stepByStepSlowMotionDisabled);
            }
            sendNotification(NotificationType.stepByStepDisabled);
        }
        updateInfo();
    }

}
