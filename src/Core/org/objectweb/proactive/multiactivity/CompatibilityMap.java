package org.objectweb.proactive.multiactivity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.body.request.Request;

public class CompatibilityMap {
	
	protected Map<String, MethodGroup> groups = new HashMap<String, MethodGroup>();
	protected Map<String, MethodGroup> methods = new HashMap<String, MethodGroup>();
	
	public CompatibilityMap(AnnotationProcessor annotProc) {
		this.groups = annotProc.groups;
		this.methods = annotProc.methods;
	}
	
	public CompatibilityMap(Class<?> clazz) {
		AnnotationProcessor annotProc = new AnnotationProcessor(clazz);
		this.groups = annotProc.groups;
		this.methods = annotProc.methods;
	}
	
	public MethodGroup getGroupOf(String methodName) {
		return methods.get(methodName);
	}
	
	public MethodGroup getGroupOf(Request method){
		return getGroupOf(method.getMethodName());
	}
	
	public boolean areCompatible(String method1, String method2){
		MethodGroup mg1 = getGroupOf(method1);
		MethodGroup mg2 = getGroupOf(method2);
		if (mg1!=null && mg2!=null) {
			return (mg1.getCompatibleWith().contains(mg2)) 
				|| (method1.equals(method2) && mg1.selfCompatible);
		} else {
			return false;
		}
	}
	
	public boolean areCompatible(Request request1, Request request2){
		return areCompatible(request1.getMethodName(), request2.getMethodName());
	}
	
	public boolean areCompatibleMethods(Collection<String> methods){
		for (String method : methods) {
			for (String other : methods) {
				if (method!=other) {
					if (!areCompatible(method, other)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
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
	
	public boolean isCompatibleWithMethods(String method, Collection<String> others){
		for (String other : others) {
			if (!areCompatible(method, other)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isCompatibleWithRequests(Request request, Collection<Request> others){
		for (Request other : others) {
			if (!areCompatible(request, other)) {
				return false;
			}
		}
		return true;
	}
}
