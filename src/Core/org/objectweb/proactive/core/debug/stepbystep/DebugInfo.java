/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.stepbystep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;


public class DebugInfo implements Serializable {

    private UniqueID activeObjectId;
    private String nodeUrl;
    private boolean stepByStepMode;
    private boolean slowMotion;
    private List<DebugBreakpointInfo> debugBreakpointInfo = new ArrayList<DebugBreakpointInfo>();
    private Set<BreakpointType> breakpointTypeFilter;

    //  -- CONSTRUCTOR -----------------------------------------------
    public DebugInfo() {
    }

    public DebugInfo(AbstractBody abstractBody) {
        this.activeObjectId = abstractBody.getID();
        this.nodeUrl = abstractBody.getNodeURL();
    }

    //  -- METHODS -----------------------------------------------
    public synchronized void setInfo(Debugger debugger) {
        debugBreakpointInfo.clear();
        for (BreakpointInfo info : debugger.getBreakpoints().values()) {
            debugBreakpointInfo.add(new DebugBreakpointInfo(info));
        }
        stepByStepMode = debugger.isStepByStepMode();
        breakpointTypeFilter = debugger.getBreakpointTypeFilter();
        slowMotion = debugger.isSlowMotionEnabled();
    }

    public synchronized String toString() {
        String res;
        res = "************************************ STATE ************************************\n" + " id = " +
            activeObjectId + "\n" + " node URL =  " + nodeUrl + "\n" + " StepBYStep ";
        if (stepByStepMode) {
            res += "activated. ";
            if (isBlockedInBreakpoint()) {
                res += "Blocked in breakpoint: \n";
                for (DebugBreakpointInfo i : debugBreakpointInfo) {
                    res += "\t[" + i.getBreakpointId() + "] " + i.getBreakpointType() + "\n";
                    if (i.getMethodName() != null) {
                        res += "\t\tmethod: " + i.getMethodName() + "\n";
                    }
                    res += "\t\tthread: " + i.getThreadName() + "\n";
                }
            } else {
                res += "Not blocked in breakpoint";
            }
            res += "\n";
            res += " Enable breakpoints: ";
            boolean first = true;
            for (BreakpointType filter : breakpointTypeFilter) {
                if (first) {
                    first = false;
                } else {
                    res += ", ";
                }
                res += filter;
            }
        } else {
            res += "not activated";
        }
        res += "\n" + "*******************************************************************************";
        return res;
    }

    public boolean isBlockedOutOfBreakpoint() {
        return isStepByStepMode() && !isBlockedInBreakpoint();
    }

    public synchronized boolean hasImmediate() {
        for (DebugBreakpointInfo i : debugBreakpointInfo) {
            if (i.isImmediate()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlockedInBreakpoint() {
        return !debugBreakpointInfo.isEmpty();
    }

    //  -- GETTERS -----------------------------------------------
    public UniqueID getActiveObjectId() {
        return activeObjectId;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public List<DebugBreakpointInfo> getDebugBreakpointInfo() {
        return debugBreakpointInfo;
    }

    public boolean isStepByStepMode() {
        return stepByStepMode;
    }

    public boolean isSlowMotion() {
        return slowMotion;
    }

    public Set<BreakpointType> getBreakpointTypeFilter() {
        return breakpointTypeFilter;
    }

}
