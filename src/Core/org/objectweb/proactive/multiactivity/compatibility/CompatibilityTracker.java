package org.objectweb.proactive.multiactivity.compatibility;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;

public class CompatibilityTracker extends StatefulCompatibilityMap {
    private Map<MethodGroup, Integer> compats = new HashMap<MethodGroup, Integer>();
    private Map<String, List<Request>> runningMethods = new HashMap<String, List<Request>>();
    private BlockingRequestQueue queue;
    private int runningCount = 0;

    public CompatibilityTracker(AnnotationProcessor annotProc, BlockingRequestQueue queue) {
        super(annotProc);

        for (MethodGroup groupName : getGroups()) {
            compats.put(groupName, 0);
        }
        
        this.queue = queue;
    }

    /*
     * adds the request to the running set adds one to the compatibility count of all groups
     * this request's group is compatible with
     * 
     * Time: O(g) -- g is the number of groups
     */
    public void addRunning(Request request) {
        String method = request.getMethodName();
        if (getGroupOf(method)!=null) {
            for (MethodGroup mg : getGroupOf(method).getCompatibleWith()) {
                compats.put(mg, compats.get(mg) + 1);
            }
        }
        if (!runningMethods.containsKey(method)) {
            runningMethods.put(method, new LinkedList<Request>());
        }
        runningMethods.get(method).add(request);
        runningCount++;
    }

    /*
     * removes the request from the running set removes one from the compatibility count of all
     * groups this request's group is compatible with
     * 
     * Time: O(g) -- g is the number of groups
     */
    public void removeRunning(Request request) {
        String method = request.getMethodName();
        if (getGroupOf(method)!=null) {
            for (MethodGroup mg : getGroupOf(method).getCompatibleWith()) {
                compats.put(mg, compats.get(mg) - 1);
            }
        }
        runningMethods.get(method).remove(request);
        runningCount--;
    }

    @Override
    public boolean isCompatibleWithExecuting(Request r) {
        return isCompatibleWithExecuting(r.getMethodName());
    }

    /*
     * Alternative for the isCompatibleWithRequests in case we check with the running ones. This
     * will return the answer in O(1) time, as opposed to O(n) worst-case time of the other.
     * Thsi works by checking if the count of compatible methods is equal to the number of
     * running methods. The count is stored for each group and updated upon addition/removal of
     * requests to/from the running set.
     * 
     * Time: O(1)
     */
    public boolean isCompatibleWithExecuting(String method) {
        if (runningCount == 0)
            return true;

        MethodGroup mg = getGroupOf(method);
        return (mg != null && compats.containsKey(mg)) && (compats.get(mg) == runningCount);
    }

    public Set<String> getExecutingMethodNameSet() {
        return runningMethods.keySet();
    }

    @Override
    public List<String> getExecutingMethodNames() {
        List<String> names = new LinkedList<String>();
        for (String m : runningMethods.keySet()) {
            for (int i = 0; i < runningMethods.get(m).size(); i++) {
                names.add(m);
            }
        }

        return names;
    }

    @Override
    public List<Request> getExecutingRequests() {
        List<Request> reqs = new LinkedList<Request>();
        for (List<Request> lrr : runningMethods.values()) {
            reqs.addAll(lrr);
        }

        return reqs;
    }

    @Override
    public List<Request> getExecutingRequestsFor(String method) {
        return (runningMethods.containsKey(method)) ? runningMethods.get(method)
                : new LinkedList<Request>();
    }

    @Override
    public Request getOldestInTheQueue() {
        return queue.getOldest();
    }

    @Override
    public List<Request> getQueueContents() {
        return queue.getInternalQueue();
    }

    @Override
    public int getNumberOfExecutingRequests() {
        return runningCount;
    }

}