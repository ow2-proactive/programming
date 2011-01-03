package org.objectweb.proactive.multiactivity;

import java.lang.reflect.Field;
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
import org.objectweb.proactive.annotation.multiactivity.Modifies;
import org.objectweb.proactive.annotation.multiactivity.Reads;

/**
 * Reads and processes the multi-activity related annotations of a class and produces 
 * two data structures that describe the compatibility of the methods of this class.
 * <br>
 * These data structures are:
 * <ul>
 * 	<li>group map -- this is a map that associates the names of the groups with the actual 
 * {@link MethodGroup}s. It can be retrieved through the {@link #getGroupNameMap()} method.</li>
 *  <li>method map -- this is a map that holds the group that each method belongs to. It can be 
 *  accessed through the {@link #getMethodNameMap()} method.</li> 
 * </ul>
 * 
 * <br>
 * For information on the multi-active annotations, please refer to the 
 * <code>org.objectweb.proactive.annotations.multiactivity</code> package.
 * @author Zsolt Istvan
 *
 */
public class AnnotationProcessor {
	private static final String CLASS_LEVEL="CLASS";
	
	private Map<String, MethodGroup> groups = new HashMap<String, MethodGroup>();
	private Map<String, MethodGroup> methods = new HashMap<String, MethodGroup>();
	private Class<?> myClass;
	
	protected Map<String, List<String>> errors = new HashMap<String, List<String>>();
	
	public AnnotationProcessor(Class<?> c) {
		myClass = c;
		
		processClassAnnotations();
		processMethodAnnotations();
	}
	
	/**
	 * Reads and processes the following types of class-level annotations:
	 * <ul>
	 *  <li>{@link DefineGroups} and {@link Group} -- these define the groups</li>
	 *  <li>{@link DefineRules} and {@link Compatible} -- these define the rules that apply between them</li>
	 * </ul>
	 */
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
	
	/**
	 * Processes method-level annotations.
	 * First it deals with group membership and method-to-method compatibility, then 
	 * with variable accesses.
	 */
	protected void processMethodAnnotations(){
		
		processCompatible();
		processReadModify();
	}
	
	/**
	 * Reads and processes the following method-level annotations:
	 * <ul>
	 *  <li>{@link Reads} -- the variables that are just read by the method</li>
	 *  <li>{@link Modifies} -- the variables that are written by the method</li>
	 * </ul>
	 */
	protected void processReadModify() {
		//TODO :D
	}

	/**
	 * Reads and processes the following method-level annotations:
	 * <ul>
	 *  <li>{@link MemberOf} -- the group the method belongs to</li>
	 *  <li>{@link Compatible} -- the additional methods it is compatible with</li>
	 * </ul>
	 */
	protected void processCompatible() {
		HashMap<String, HashSet<String>> compMap = new HashMap<String, HashSet<String>>();
		
		//go through each public method of a class
		for (Method method : myClass.getMethods()) {
			//check what group is it part of
			MemberOf group = method.getAnnotation(MemberOf.class);
			if (group!=null) {
				MethodGroup mg = groups.get(group.value());
				methods.put(method.getName(), mg);
			}
			
			//other compatible declarations -- put them into a map
			Compatible comp = method.getAnnotation(Compatible.class);
			if (comp!=null) {
				HashSet<String> comps = new HashSet<String>();
				for (String other : comp.value()) {
					comps.add(other);

				}
				compMap.put(method.getName(), comps);
			}
		}
		
		//go through methods that declared compatibilities
		for (String method : compMap.keySet()) {
			boolean selfCompatible = compMap.get(method).contains(method);
			
			//create a group for this method -- maybe extend its already existing group
			MethodGroup newGroup = new MethodGroup(method, selfCompatible);
			if (groups.containsKey(method)) { 
				newGroup.addCompatibleWith(groups.get(method).getCompatibleWith());
			}
			
			methods.put(method, newGroup);
			groups.put(newGroup.name, newGroup);
		}
		
		//set compatibilities between groups, based on the method-level compatible annotations
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
	
	/**
	 * Returns the invalid references (group names, method names, variable names that are not defined 
	 * in the class but appear in the annotations).
	 * @return a map that holds locations and the related lists of the invalid references. A location 
	 * can be a method name or "CLASS" in case the error is in the class level annotations.
	 */
	public Map<String, List<String>> getInvalidReferences(){
		return errors;
	}
	
	/**
	 * Returns true if the annotations contain references (group names, method names, variable names)
	 * that are not defined in the class.
	 * @return
	 */
	public boolean hasInvalidReferences(){
		return errors.keySet().size()>0;
	}
	
	/**
	 * Returns a map that maps the group names to the method groups.
	 * @return
	 */
	public Map<String, MethodGroup> getGroupNameMap() {
		return groups;
	}

	/**
	 * Returns a map that pairs each method name with a method group.
	 * @return
	 */
	public Map<String, MethodGroup> getMethodNameMap() {
		return methods;
	}
	
	/**
	 * Returns true if the processed class has a variable named like the parameter.
	 * @param ref variable name
	 * @return
	 */
	private boolean classHasVariable(String what) {
		Field[] meths = myClass.getDeclaredFields();
		for (int i=0; i<meths.length; i++) {
			if (meths[i].getName().equals(what)) return true;
		}
		return false;
	}

}
