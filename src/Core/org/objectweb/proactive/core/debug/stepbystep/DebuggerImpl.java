/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.stepbystep;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ObjectForSynchro;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class DebuggerImpl implements Debugger {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;

    private static Logger logger = ProActiveLogger.getLogger(Loggers.DEBUGGER);

    /** internal use only : for wait/notify  */
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

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public DebuggerImpl() {
        initBreakpointTypes();
        disableBreakpointTypes(new BreakpointType[] { BreakpointType.SendRequest });
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
     * @see org.objectweb.proactive.core.debug.Debugger#initBreakpointTypes()
     */
    public void initBreakpointTypes() {
        disableAllBreakpointTypes();
        enableBreakpointTypes(BreakpointType.values());
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#addBreakpointTypes(BreakpointType[])
     */
    public void enableBreakpointTypes(BreakpointType[] types) {
        for (BreakpointType type : types) {
            breakpointTypeFilter.add(type);
        }
        updateInfo();
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#removeBreakpointTypes(BreakpointType[])
     */
    public void disableBreakpointTypes(BreakpointType[] types) {
        for (BreakpointType type : types) {
            breakpointTypeFilter.remove(type);
        }
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#removeBreakpointTypes(BreakpointType[])
     */
    public void disableAllBreakpointTypes() {
        breakpointTypeFilter.clear();
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#breakpoint(BreakpointType)
     */
    public void breakpoint(BreakpointType type, Request request) {
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
     * @see org.objectweb.proactive.core.debug.Debugger#resume()
     */
    public void resume() {
        setStepByStep(false);
        breakpoints.clear();
        unblock();
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#nextStep()
     */
    public void nextStep() {
        breakpoints.clear();
        unblock();
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#nextStep(long)
     */
    public void nextStep(long id) {
        breakpoints.remove(id);
        unblock();
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#nextStep(Collection)
     */
    public void nextStep(Collection<Long> ids) {
        breakpoints.remove(ids);
        unblock();
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#slowMotion(long)
     */
    public void slowMotion(long slowMotionDelay) {
        slowMotion(slowMotionDelay, true);
    }

    /**
     * @see org.objectweb.proactive.core.debug.Debugger#slowMotion(long,
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
     * @see org.objectweb.proactive.core.debug.Debugger#disableSlowMotion()
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
                mbean.sendNotification(type);
            }
        }
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
