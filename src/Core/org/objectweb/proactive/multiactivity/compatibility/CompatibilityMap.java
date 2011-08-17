package org.objectweb.proactive.multiactivity.compatibility;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.body.request.Request;

/**
 * This class can be used to query information about method groups ({@link Group}) and compatibility relations ({@link Compatible}) in a class. 
 * It uses the {@link AnnotationProcessor} to extract this information.  
 * @author  Zsolt Istvan
 */
public class CompatibilityMap {
	
	private Map<String, MethodGroup> groups = new HashMap<String, MethodGroup>();
	private Map<String, MethodGroup> membership = new HashMap<String, MethodGroup>();
	
	/**
	 * Create a compatibility map using an already existing annotation processor.
	 * @param annotProc
	 */
	public CompatibilityMap(AnnotationProcessor annotProc) {
		this.groups = annotProc.getMethodGroups();
		this.membership = annotProc.getMethodMemberships();
	}
	
	/**
	 * Create a compatibility map of a class.
	 * @param clazz
	 */
	public CompatibilityMap(Class<?> clazz) {
		AnnotationProcessor annotProc = new AnnotationProcessor(clazz);
		this.groups = annotProc.getMethodGroups();
		this.membership = annotProc.getMethodMemberships();
	}
	
	/**
	 * Returns the method group a request belongs to.
	 * @param method
	 * @return
	 */
	public MethodGroup getGroupOf(Request method){
		return membership.get(MethodGroup.getNameOf(method));
	}
	
	/**
	 * Returns all method groups defined for this class (and inherited ones as well).
	 * @return
	 */
	public Collection<MethodGroup> getGroups() {
	    return groups.values();
	}	
	
	/**
	 * Checks whether the two requests are compatible or not. If a condition function was
	 * defined for these two requests, it is evaluated. 
	 * <br>
	 * For details on deciding compatibility see {@link MethodGroup}.
	 */
	public boolean areCompatible(Request request1, Request request2){
		MethodGroup mg1 = getGroupOf(request1);
        MethodGroup mg2 = getGroupOf(request2);

        if (mg1!=null && mg2!=null) {
            return mg1.isCompatible(request1, mg2, request2);
        } else {
            return false;
        }
	}
	
	/**
	 * Returns true if all requests are pairwise compatible.
	 * <br>
	 * For details on deciding compatibility see {@link MethodGroup}.
	 */
	public boolean areCompatibleRequests(Collection<Request> requests){
		for (Request request : requests) {
			for (Request other : requests) {
				if (request!=other) {
					if (!areCompatible(request, other)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns true if "request" is compatible with all other requests. The requests in the collection are not checked for 
	 * compatibility between themselves.
	 * <br>
	 * For details on deciding compatibility see {@link MethodGroup}.
	 * 
	 * @param request the request to check against the others
	 * @param others collection of requests
	 */
	public boolean isCompatibleWithRequests(Request request, Collection<Request> others){
		for (Request other : others) {
			if (!areCompatible(request, other)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks the "request" against a list of others. It stops when encountering an incompatibility, and returns the index of the last compatible request.
	 * <br>
	 * For details on deciding compatibility see {@link MethodGroup}.
	 * 
	 * @param request the request to check against the others
	 * @param others collection of requests
	 * @return index of the last request the checked one is compatible with:
	 * <ul>
	 *  <li> -1 -- the request is not compatible with the first element from the list
	 *  <li> N -- the request is compatible with all elements up until the Nth
	 *  <li> size()-1 -- the request is compatible with all
	 * <ul>
	 */
	public int getIndexOfLastCompatibleWith(Request request, List<Request> others){
	    for (int i=0; i<others.size(); i++) {
            if (!areCompatible(request, others.get(i))) {
                return i-1;
            }
        }
        return others.size()-1;
    }
}
