package org.objectweb.proactive.multiactivity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.body.request.Request;

/**
 * This class represents a static compatibility map built based on the annotations found in 
 * the underlying class. It has methods for querying these relations.
 * @author Izso
 *
 */
public class MultiActiveCompatibilityMap {
	private final Map<String, List<String>> map;
	
	/**
	 * Create the map from a data structure
	 * @param map
	 */
	public MultiActiveCompatibilityMap(Map<String, List<String>> map) {
		this.map = map;
	}
	
	/**
	 * Create the map based on a class, that will be processed by the 
	 * {@link MultiActiveAnnotationProcessor} internally.
	 * @param forClass
	 * @return
	 */
	public static MultiActiveCompatibilityMap getFor(Class<?> forClass) {
		return (new MultiActiveAnnotationProcessor(forClass)).getCompatibilityMap();
	}
	
	/**
	 * Returns the list of method names that are compatible with the one given
	 * as parameter
	 * @param method
	 * @return
	 */
	public List<String> getCompatibilityListOf(String method) {
		return (map.containsKey(method)) ? map.get(method) : new LinkedList<String>();
	}
	
	/**
	 * Returns the list of method names that are compatible with the method contained
	 * by the request given as parameter
	 * @param request
	 * @return
	 */
	public List<String> getCompatibilityListOf(Request request) {
		return getCompatibilityListOf(request.getMethodName());
	}
	
	/**
	 * Returns true if the two method names are compatible (i.e. they can run in parallel)
	 * @param method1
	 * @param method2
	 * @return
	 */
	public boolean areCompatible(String method1, String method2) {
		return (map.containsKey(method1) && map.containsKey(method2))
				&& (map.get(method1).contains(method2));
	}
	
	/**
	 * Returns true if the two requests are compatible (i.e. they can run in parallel)
	 * @param method1
	 * @param method2
	 * @return
	 */
	public boolean areCompatible(Request request1, Request request2) {
		return areCompatible(request1.getMethodName(), request2.getMethodName());
	}
	
	/**
	 * Returns true if all method names are compatible pairwise (i.e. they can run in parallel)
	 * @param methods
	 * @return
	 */
	public boolean areAllCompatibleNames(List<String> methods) {
		for (int i=0; i<methods.size(); i++) {
			for (int j=i; i<methods.size(); j++) {
				if (!areCompatible(methods.get(i), methods.get(j))) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns true if all requests are compatible pairwise (i.e. they can run in parallel)
	 * @param requests
	 * @return
	 */
	public boolean areAllCompatibleRequests(List<Request> requests) {
		for (int i=0; i<requests.size(); i++) {
			for (int j=i; i<requests.size(); j++) {
				if (!areCompatible(requests.get(i), requests.get(j))) return false;
			}
		}
		
		return true;
		
	}
	
	/**
	 * Returns true if the given method is compatible with all the others from the list.
	 * The compatibility of the elements of the list is not checked.
	 * @param method
	 * @param others
	 * @return
	 */
	public boolean isCompatibleWithAllNames(String method, Collection<String> others) {
		for (String other : others) {
			if (!areCompatible(method, other)) return false;
		}
		
		return true;
	}
	
	/**
	 * Returns true if the given request is compatible with all the others from the list.
	 * The compatibility of the elements of the list is not checked.
	 * @param request
	 * @param others
	 * @return
	 */
	public boolean isCompatibleWithAllRequests(Request request, Collection<Request> others) {
		for (Request other : others) {
			if (!areCompatible(request, other)) return false;
		}
		
		return true;		
	}
	
	
	
}
