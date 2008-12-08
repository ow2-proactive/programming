package org.objectweb.proactive.core.debug.stepbystep;

import java.io.Serializable;


public enum BreakpointType implements Serializable {

    NewService("New Service", false), NewImmediateService("New Immediate Service", true), EndService(
            "End Service", false), EndImmediateService("End Immediate Service", true), SendRequest(
            "Send Request");

    private String name;
    private boolean immediate;

    private BreakpointType(String name) {
        this(name, false);
    }

    private BreakpointType(String name, boolean isImmediate) {
        this.name = name;
        this.immediate = isImmediate;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public String toString() {
        return name;
    }

    /** Return all breakpoint types */
    public static BreakpointType[] getAllTypes() {
        return new BreakpointType[] { SendRequest, NewService, EndService, NewImmediateService,
                EndImmediateService };
    }
}
