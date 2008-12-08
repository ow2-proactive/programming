package org.objectweb.proactive.core.debug.stepbystep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;


public class DebugInfo implements Serializable {

    private static final long serialVersionUID = -2729514298527837687L;
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
