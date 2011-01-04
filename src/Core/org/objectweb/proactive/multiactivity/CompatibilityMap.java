package org.objectweb.proactive.multiactivity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.body.request.Request;

/**
 * This class can be used to access information about groups and compatibility relation
 * between methods in a class. It has an underlying {@link AnnotationProcessor} to extract 
 * this information.
 * <br>
 * The methods of this class are defined both for parameters of type String
 * (for method names) and of type {@link Request} 
 * @author Zsolt Istvan
 *
 */
public class CompatibilityMap {
	
	protected Map<String, MethodGroup> groups = new HashMap<String, MethodGroup>();
	protected Map<String, MethodGroup> methods = new HashMap<String, MethodGroup>();
	
	public CompatibilityMap(AnnotationProcessor annotProc) {
		this.groups = annotProc.getGroupNameMap();
		this.methods = annotProc.getMethodNameMap();
	}
	
	public CompatibilityMap(Class<?> clazz) {
		AnnotationProcessor annotProc = new AnnotationProcessor(clazz);
		this.groups = annotProc.getGroupNameMap();
		this.methods = annotProc.getMethodNameMap();
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
			return mg1.getCompatibleWith().contains(mg2) || mg2.getCompatibleWith().contains(mg1);
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
