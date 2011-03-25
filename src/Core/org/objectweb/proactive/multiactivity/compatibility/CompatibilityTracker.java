package org.objectweb.proactive.multiactivity.compatibility;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;

public class CompatibilityTracker extends StatefulCompatibilityMap {
    
    private HashMap<MethodGroup, Set<Request>> runningGroups = new HashMap<MethodGroup, Set<Request>>();
    private Set<Request> running = new HashSet<Request>();
    private BlockingRequestQueue queue;
    private int runningCount = 0;

    public CompatibilityTracker(AnnotationProcessor annotProc, BlockingRequestQueue queue) {
        super(annotProc);

        for (MethodGroup group : getGroups()) {
            runningGroups.put(group, new HashSet<Request>());
        }
        //methods without a group
        runningGroups.put(null, new HashSet<Request>());
        
        this.queue = queue;
    }

    /*
     * adds the request to the running set adds one to the compatibility count of all groups
     * this request's group is compatible with
     * 
     * Time: O(g) -- g is the number of groups
     */
    public void addRunning(Request request) {
        runningCount++;
        running.add(request);
        runningGroups.get(getGroupOf(request)).add(request);
    }

    /*
     * removes the request from the running set removes one from the compatibility count of all
     * groups this request's group is compatible with
     * 
     * Time: O(g) -- g is the number of groups
     */
    public void removeRunning(Request request) {
        runningCount--;
        running.remove(request);
        runningGroups.get(getGroupOf(request)).remove(request);
    }

    @Override
    public boolean isCompatibleWithExecuting(Request r) {
      if (runningCount == 0)
          return true;

      MethodGroup reqGroup = getGroupOf(r);
      if (reqGroup==null) {
          return false;
      }
      
      for (MethodGroup otherGroup : runningGroups.keySet()) {
          if (runningGroups.get(otherGroup).size()>0) {
              
              if (reqGroup.canCompareWith(otherGroup)) {
                  
                  for (Request other : runningGroups.get(otherGroup)) {
                      if (!reqGroup.isCompatible(r, otherGroup, other)) {
                          return false;
                      }
                  }
                      
              } else if (!reqGroup.isCompatibleWith(otherGroup)) { 
                  return false;
              }
          }
      }
          
      return true;
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
//    private boolean isCompatibleWithExecuting(String method) {
//        if (runningCount == 0)
//            return true;
//
//        MethodGroup mg = getGroupOf(method);
//        return (mg != null && compats.containsKey(mg)) && (compats.get(mg) == runningCount);
//    }

//    public Set<String> getExecutingMethodNameSet() {
//        return runningMethods.keySet();
//    }

//    @Override
//    public List<String> getExecutingMethodNames() {
//        List<String> names = new LinkedList<String>();
//        for (String m : runningMethods.keySet()) {
//            for (int i = 0; i < runningMethods.get(m).size(); i++) {
//                names.add(m);
//            }
//        }
//
//        return names;
//    }

    @Override
    public Collection<Request> getExecutingRequests() {
        /*List<Request> reqs = new LinkedList<Request>();
        for (List<Request> lrr : runningMethods.values()) {
            reqs.addAll(lrr);
        }

        return reqs;*/
        return running;
    }

//    @Override
//    public List<Request> getExecutingRequestsFor(String method) {
//        return (runningMethods.containsKey(method)) ? runningMethods.get(method)
//                : new LinkedList<Request>();
//    }

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