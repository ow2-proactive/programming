package org.objectweb.proactive.multiactivity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.body.request.Request;

/**
 * TODO: comment
 * @author Izso
 *
 */
public class MultiActiveCompatibilityMap {
	private final Map<String, List<String>> map;
	
	public MultiActiveCompatibilityMap(Map<String, List<String>> map) {
		this.map = map;
	}
	
	public static MultiActiveCompatibilityMap getFor(Class<?> forClass) {
		return (new MultiActiveAnnotationProcessor(forClass)).getCompatibilityMap();
	}
	
	public List<String> getCompatibilityListOf(String method) {
		return (map.containsKey(method)) ? map.get(method) : new LinkedList<String>();
	}
	
	public List<String> getCompatibilityListOf(Request request) {
		return getCompatibilityListOf(request.getMethodName());
	}
	
	public boolean areCompatible(String method1, String method2) {
		return (map.containsKey(method1) && map.containsKey(method2))
				&& (map.get(method1).contains(method2));
	}
	public boolean areCompatible(Request request1, Request request2) {
		return areCompatible(request1.getMethodName(), request2.getMethodName());
	}
	
	public boolean areAllCompatibleNames(List<String> methods) {
		for (int i=0; i<methods.size(); i++) {
			for (int j=i; i<methods.size(); j++) {
				if (!areCompatible(methods.get(i), methods.get(j))) return false;
			}
		}
		
		return true;
	}
	public boolean areAllCompatibleRequests(List<Request> requests) {
		for (int i=0; i<requests.size(); i++) {
			for (int j=i; i<requests.size(); j++) {
				if (!areCompatible(requests.get(i), requests.get(j))) return false;
			}
		}
		
		return true;
		
	}
	
	public boolean isCompatibleWithAllNames(String method, Collection<String> others) {
		for (String other : others) {
			if (!areCompatible(method, other)) return false;
		}
		
		return true;
	}
	
	public boolean isCompatibleWithAllRequests(Request request, Collection<Request> others) {
		for (Request other : others) {
			if (!areCompatible(request, other)) return false;
		}
		
		return true;		
	}
	
	
	
}
