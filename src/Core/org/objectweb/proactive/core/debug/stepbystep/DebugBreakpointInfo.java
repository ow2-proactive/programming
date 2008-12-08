package org.objectweb.proactive.core.debug.stepbystep;

import java.io.Serializable;


public class DebugBreakpointInfo implements Serializable {
    private static final long serialVersionUID = -3378756015411167898L;
    private BreakpointType breakpointType;
    private String threadName;
    private String methodName;
    private long breakpointId;

    public DebugBreakpointInfo(BreakpointInfo breakpointInfo) {
        breakpointType = breakpointInfo.getType();
        threadName = breakpointInfo.getThread().getName();
        if (breakpointInfo.getRequest() != null) {
            methodName = breakpointInfo.getRequest().getMethodName();
        }
        breakpointId = breakpointInfo.getBreakpointId();
    }

    public BreakpointType getBreakpointType() {
        return breakpointType;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isImmediate() {
        return breakpointType.isImmediate();
    }

    public long getBreakpointId() {
        return breakpointId;
    }

}