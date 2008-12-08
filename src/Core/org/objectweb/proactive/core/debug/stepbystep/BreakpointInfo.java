package org.objectweb.proactive.core.debug.stepbystep;

import java.io.Serializable;

import org.objectweb.proactive.core.body.request.Request;


public class BreakpointInfo implements Serializable {

    private BreakpointType type;

    private Thread thread;

    private Request request;

    public BreakpointInfo(BreakpointType type, Thread thread, Request request) {
        this.type = type;
        this.thread = thread;
        this.request = request;
    }

    public BreakpointType getType() {
        return type;
    }

    public Thread getThread() {
        return thread;
    }

    public Request getRequest() {
        return request;
    }

    public long getBreakpointId() {
        return thread.getId();
    }

}
