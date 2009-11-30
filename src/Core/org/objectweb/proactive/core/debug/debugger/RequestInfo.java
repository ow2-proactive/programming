package org.objectweb.proactive.core.debug.debugger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;


public class RequestInfo implements Serializable {

    private static final long serialVersionUID = -3263437546419006443L;
    private String senderID;
    private long sequenceNumber;

    private String methodCalled;
    private List<String> parameters = new ArrayList<String>();
    private String returnType;

    public RequestInfo(Request r) {
        // Sender
        if (r.getSender() != null) {
            senderID = r.getSender().getID().getCanonString();
        } else {
            senderID = "no sender";
        }
        sequenceNumber = r.getSequenceNumber();

        // MethodCalled
        if (r.getMethodName() != null) {
            methodCalled = r.getMethodName();
        } else {
            methodCalled = "not methodName";
        }
        if (r.getMethodCall() != null) {
            int sizeOfParams = r.getMethodCall().getNumberOfParameter();
            for (int i = 0; i < sizeOfParams; i++) {
                parameters.add(r.getParameter(i).getClass().getSimpleName());
            }
        }
        if (r.getMethodCall().getReifiedMethod() != null) {
            returnType = r.getMethodCall().getReifiedMethod().getReturnType().getSimpleName();
        } else {
            returnType = "no reifiedMethod";
        }
    }

    //
    // -- GETTERS AND SETTERS -----------------------------------------------
    //
    public String getMethodCalled() {
        return methodCalled;
    }

    public String getSenderID() {
        return senderID;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

}
