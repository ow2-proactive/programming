package org.objectweb.proactive.multiactivity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;

public class AnnotationProcessor {
	private static final String CLASS_LEVEL="CLASS";
	
	protected Map<String, MethodGroup> groups = new HashMap<String, MethodGroup>();
	protected Map<String, MethodGroup> methods = new HashMap<String, MethodGroup>();
	private Class<?> myClass;
	
	protected Map<String, List<String>> errors = new HashMap<String, List<String>>();
	
	public AnnotationProcessor(Class<?> c) {
		myClass = c;
		
		processClassAnnotations();
		processMethodAnnotations();
	}
	
	protected void processClassAnnotations(){
		//if there are any groups defined
		if (myClass.getAnnotation(DefineGroups.class)!=null){
			DefineGroups defg = myClass.getAnnotation(DefineGroups.class);
			
			//create the descriptor
			for (Group g : defg.value()) {
				MethodGroup mg = new MethodGroup(g.name(), g.selfCompatible());
				groups.put(g.name(), mg);
			}
			
			//if there are rules defined
			if (myClass.getAnnotation(DefineRules.class)!=null) {
				DefineRules defr = myClass.getAnnotation(DefineRules.class);
				for (Compatible c : defr.value()) {
					
					for (String group : c.value()) {
						for (String other : c.value()) {
							if (groups.containsKey(group) && groups.containsKey(other)) {
								groups.get(group).addCompatibleWith(groups.get(other));
								groups.get(other).addCompatibleWith(groups.get(group));
							} else {
								if (!groups.containsKey(group)) {
									addError(CLASS_LEVEL, group);
								}
								if (!groups.containsKey(other)) {
									addError(CLASS_LEVEL, other);
								}
							}
						}	
					}

				}
			}
			
		}
	}
	
	protected void processMethodAnnotations(){
		
		processCompatible();
		processReadModify();
	}
	
	protected void processReadModify() {
		//TODO :D
	}

	protected void processCompatible() {
		HashMap<String, HashSet<String>> compMap = new HashMap<String, HashSet<String>>();
		
		for (Method method : myClass.getMethods()) {
			MemberOf group = method.getAnnotation(MemberOf.class);
			if (group!=null) {
				methods.put(method.getName(), groups.get(group.value()));
			}
			
			Compatible comp = method.getAnnotation(Compatible.class);
			if (comp!=null) {
				HashSet<String> comps = new HashSet<String>();
				for (String other : comp.value()) {
					comps.add(other);
				}
				compMap.put(method.getName(), comps);
			}
		}
		
		for (String method : compMap.keySet()) {
			boolean selfCompatible = compMap.get(method).contains(method);
			
			MethodGroup newGroup = (groups.containsKey(method)) ? 
					new MethodGroup(groups.get(method), method, selfCompatible) : 
						new MethodGroup(method, selfCompatible);
					
			methods.put(method, newGroup);
			groups.put(newGroup.name, newGroup);
		}
		
		for (String method : compMap.keySet()) {
			for (String other : compMap.get(method)) {
				if (compMap.containsKey(other) && compMap.get(other).contains(method)) {
					methods.get(method).addCompatibleWith(methods.get(other));
					methods.get(other).addCompatibleWith(methods.get(method));
				} else {
					addError(method, other);
				}
			}
		}
	}

	private void addError(String where, String what) {
		if (!errors.containsKey(where)) {
			errors.put(where, new LinkedList<String>());
		}
		
		errors.get(where).add(what);		
	}
	
	/*
	 * public Map<String, List<String>> getInvalidReferences(){
		if (invalidReferences==null) {
			invalidReferences = new HashMap<String, List<String>>();
			for (String m : methodInfos.keySet()){
				AnnotatedMethod ann = methodInfos.get(m);
				for (String ref : ann.getCompatibleWith()) {
					checkReferenceCorrectness(m, ref);
				}
				for (String ref : ann.getModifies()) {
					checkReferenceCorrectness(m, ref);
				}
				for (String ref : ann.getReads()) {
					checkReferenceCorrectness(m, ref);
				}
				for (String group : ann.getGroups()) {
					checkReferenceCorrectness(m, group);
				}
			}
			
			for (String group : methodGroups.keySet()) {
				for (String compG : methodGroups.get(group).getCompatibleGroups()) {
					checkReferenceCorrectness("", compG);
				}
				
				for (String compM : methodGroups.get(group).getMembers()) {
					checkReferenceCorrectness("", compM);
				}
			}
		}
		return invalidReferences;
	}

	private void checkReferenceCorrectness(String sourceMethod, String reference) {
		if (!reference.startsWith("#") && !classHasPublicMethod(reference) 
				&& !classHasVariable(reference) && !methodGroups.keySet().contains(reference)) {
			if (invalidReferences.get(sourceMethod)==null) {
				invalidReferences.put(sourceMethod, new LinkedList<String>());
			}
			invalidReferences.get(sourceMethod).add(reference);
		}
	}
	
	private boolean classHasVariable(String ref) {
		Field[] meths = thisClass.getDeclaredFields();
		for (int i=0; i<meths.length; i++) {
			if (meths[i].getName().equals(ref)) return true;
		}
		return false;
	}

	private boolean classHasPublicMethod(String ref) {
		Method[] meths = thisClass.getMethods();
		for (int i=0; i<meths.length; i++) {
			if (meths[i].getName().equals(ref)) return true;
		}
		return false;
	}

	public boolean areAnnotationsCorrect(){
		if (invalidReferences==null) {
			invalidReferences = getInvalidReferences();
		}
		return invalidReferences.keySet().size()==0;
	}
	*/
}
