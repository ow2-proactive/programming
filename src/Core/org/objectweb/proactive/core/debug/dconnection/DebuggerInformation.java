package org.objectweb.proactive.core.debug.dconnection;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;


public class DebuggerInformation implements Serializable {

    private static final long serialVersionUID = -2470754430058574000L;

    private Node debuggerNode;
    private int debuggeePort;

    public DebuggerInformation(Node node, int port) {
        this.debuggerNode = node;
        this.debuggeePort = port;
    }

    public Node getDebuggerNode() {
        return debuggerNode;
    }

    public int getDebuggeePort() {
        return debuggeePort;
    }

    public String toString() {
        return "#<DebugInfo node: " + debuggerNode + " port: " + debuggeePort + ">";
    }

}
