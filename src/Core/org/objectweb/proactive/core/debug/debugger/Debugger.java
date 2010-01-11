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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;


public interface Debugger extends Serializable {

    /**
     * enable all breakpointType used by stepByStep
     */
    public void initBreakpointTypes();

    /**
     * enable some breakpointTypes used by stepByStep
     * @param types, a table of BreakpointType
     */
    public void updateBreakpointTypes(Map<String, Boolean> types);

    public Set<BreakpointType> getBreakpointTypeFilter();

    /**
     * define a breakpoint
     */
    public void breakpoint(BreakpointType type, Request request);

    /**
     * Resume all the threads blocked in current breakpoint.
     * The next active breakpoints will block them if the step by step mode is enable.
     */
    public void nextStep();

    /**
     * Resume one thread blocked in current breakpoint.
     * The next active breakpoints will block them if the step by step mode is enable.
     * @param id the id of the thread.
     */
    public void nextStep(long id);

    /**
     * Resume some threads blocked in current breakpoint.
     * The next active breakpoints will block them if the step by step mode is enable.
     * @param ids the collection of the threads.
     */
    public void nextStep(Collection<Long> ids);

    /**
     * resume the serving
     */
    public void resume();

    /**
     * set the time between every breakpoint
     *
     * @param slowMotionDelay
     *            the time in millisecond
     */
    public void slowMotion(long slowMotionDelay);

    /**
     * set the time between every breakpoint
     *
     * @param slowMotionDelay
     *            the time in millisecond
     * @param useImmediatly
     *            true to using slowMotion immediately and false to wait the next
     *            breakpoint
     */
    public void slowMotion(long slowMotionDelay, boolean useImmediatly);

    /**
     * to disable slow motion mode
     */
    public void disableSlowMotion();

    /**
     * to know if the debugger is enable
     *
     * @return true if enable, false otherwise
     */
    public boolean isStepByStepMode();

    /**
     * to know if the debugger is in SlowMotion mode or not
     * @return true if enable, false otherwise
     */
    public boolean isSlowMotionEnabled();

    /**
     * return the state of the ActivableObject
     */
    public DebugInfo getDebugInfo();

    public Map<Long, BreakpointInfo> getBreakpoints();

    /**
     * set the Body to attach the debugger to
     *
     * @param target the Body to attach the debugger to
     */
    public void setTarget(AbstractBody target);

    /**
     * set enable or disable the debugger
     *
     * @param activated
     *            true for enable, false for disable
     */
    public void setStepByStep(boolean activated);

    //
    // -- EXTENDED DEBUGGER ------------------------------------------------
    //
    /**
     * define a sendRequest breakpoint
     */
    public void breakpoint(BreakpointType type, UniversalBody destinationBody);

    /**
     * enable the extended debugger feature
     */
    public void enableExtendedDebugger();

    /**
     * disable the extended debugger feature
     */
    public void disableExtendedDebugger();

    /**
     * unblock for the debugger connection
     */
    public void unblockConnection();

    /**
     * @return the request queue of the body
     */
    public RequestQueueInfo getRequestQueueInfo();

    /**
     * @param sequenceNumber
     */
    public void moveUpRequest(final long sequenceNumber);

    /**
     * @param sequenceNumber
     */
    public void moveDownRequest(final long sequenceNumber);
}