package org.objectweb.proactive.core.debug.debugger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;


public class RequestQueueInfo implements Serializable {

    private static final long serialVersionUID = -8635066918170945239L;
    private List<RequestInfo> requestQueueInfo = new ArrayList<RequestInfo>();
    private RequestInfo currentRequestInfo = null;
    private List<RequestInfo> requestsToServe = new ArrayList<RequestInfo>();

    public RequestQueueInfo(List<Request> requestQueue, Request currentRequest) {
        // requestQueue
        for (Request r : requestQueue) {
            requestQueueInfo.add(new RequestInfo(r));
        }

        //currentRequest
        if (currentRequest != null) {
            this.currentRequestInfo = new RequestInfo(currentRequest);
        }
    }

    public List<RequestInfo> getRequestQueueInfo() {
        return requestQueueInfo;
    }

    public RequestInfo getCurrentRequest() {
        return currentRequestInfo;
    }

    public List<RequestInfo> getRequestsToServe() {
        return requestsToServe;
    }
}
