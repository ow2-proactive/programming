package org.objectweb.proactive.multiactivity.compatibility;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.annotation.multiactivity.Modifies;
import org.objectweb.proactive.annotation.multiactivity.Reads;
import org.objectweb.proactive.core.util.log.Loggers;

/**
 * Reads and processes the multi-activity related annotations of a class and produces 
 * two data structures that describe the compatibility of the methods of this class.
 * <br>
 * These data structures are:
 * <ul>
 * 	<li>group map -- this is a map that associates the names of the groups with the actual 
 * {@link MethodGroup}s. It can be retrieved through the {@link #getMethodGroups()} method.</li>
 *  <li>method map -- this is a map that holds the group that each method belongs to. It can be 
 *  accessed through the {@link #getMethodMemberships()} method.</li> 
 * </ul>
 * 
 * <br>
 * For information on the multi-active annotations, please refer to the 
 * <code>org.objectweb.proactive.annotations.multiactivity</code> package.
 * @author Zsolt Istvan
 *
 */
public class AnnotationProcessor {
	//text used to define the place and type of an error in the annotations
	protected static final String LOC_CLASS="at class";
	protected static final String LOC_METHOD="at method";
	protected static final String UNDEF_GROUP="undefined group";
	protected static final String DUP_GROUP="duplicate group definition";
	protected static final String UNDEF_METHOD="unresolvable method name";
	protected static final String AD_HOC_GROUP="AD_HOC_";
	
	protected Logger logger = Logger.getLogger(Loggers.MAO);
	
	//group names -> method groups
	private Map<String, MethodGroup> groups = new HashMap<String, MethodGroup>();
	//method name -> method group in which it is member
	private Map<String, MethodGroup> methods = new HashMap<String, MethodGroup>();
	
	//class that is processed
	private Class<?> processedClass;
	
	//set of the method name inside a class -- used for error checking -- populated only on need
	private HashSet<String> classMethods;
	
	//list of errors
	protected List<String> errorMessages = new LinkedList<String>();
	
	public AnnotationProcessor(Class<?> c) {
		processedClass = c;
		
		//create the inheritance list of the class (in descending order of "age")
		List<Class> parents = new LinkedList<Class>();
		parents.add(c);
		Class<?> p;
		while ((p = parents.get(0).getSuperclass())!=null) {
			parents.add(0, p);
		}
		
		Iterator<Class> i = parents.iterator();
		
		//process group and rule definitions starting from the oldest class.
		while(i.hasNext()) {
			processGroupsAndRules(i.next());
		}
		
		processAnnotationsAtMethods();
	}
	
	/**
	 * Reads and processes the following types of class-level annotations:
	 * <ul>
	 *  <li>{@link DefineGroups} and {@link Group} -- these define the groups</li>
	 *  <li>{@link DefineRules} and {@link Compatible} -- these define the rules that apply between them</li>
	 * </ul>
	 */
	protected void processGroupsAndRules(Class<?> processedClass){
		Annotation[] declaredAnns = processedClass.getDeclaredAnnotations();
		Annotation groupDefAnn = null;
		Annotation compDefAnn = null;
		
		for (Annotation a : declaredAnns) {
			if (groupDefAnn==null && a.annotationType().equals(DefineGroups.class)) {
				groupDefAnn = a;
			}			
			if (compDefAnn==null && a.annotationType().equals(DefineRules.class)) {
				compDefAnn = a;
			}
			
			if (compDefAnn!=null && groupDefAnn!=null) {
				break;
			}
		}
		
		//if there are groups
		if (groupDefAnn!=null){
			for (Group g : ((DefineGroups) groupDefAnn).value()) {
				if (!groups.containsKey(g.name())) {
					MethodGroup mg = new MethodGroup(g.name(), g.selfCompatible(), g.parameter(), g.condition());
					groups.put(g.name(), mg);
				} else {
					addError(LOC_CLASS, processedClass.getCanonicalName(), DUP_GROUP, g.name());
				}
			}
		}	
		
		// if there are rules defined
		if (compDefAnn!=null) {
			for (Compatible c : ((DefineRules) compDefAnn).value()) {
				String comparator = c.condition();
				for (String group : c.value()) {
					for (String other : c.value()) {
						if (groups.containsKey(group) && groups.containsKey(other) && !other.equals(group)) {
							groups.get(group).addCompatibleWith(groups.get(other));
							groups.get(group).setComparatorFor(other, comparator);
							groups.get(other).addCompatibleWith(groups.get(group));
							groups.get(other).setComparatorFor(group, comparator);

						} else {
							if (!groups.containsKey(group)) {
								addError(LOC_CLASS, processedClass.getCanonicalName(), UNDEF_GROUP, group);
							}
							if (!groups.containsKey(other)) {
								addError(LOC_CLASS, processedClass.getCanonicalName(), UNDEF_GROUP, other);
							}
						}
					}
				}

			}
		}

	}
	

	/*
	 * Reads and processes the following method-level annotations:
	 * <ul>
	 *  <li>{@link Reads} -- the variables that are just read by the method</li>
	 *  <li>{@link Modifies} -- the variables that are written by the method</li>
	 * </ul>
	 */
	/*protected void processReadModify() {
		//map method names to variables
		HashMap<String, HashSet<String>> reads = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> modifs = new HashMap<String, HashSet<String>>();
		
		//map variables to referencing method names
		HashMap<String, HashSet<String>> readVars = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> modifVars = new HashMap<String, HashSet<String>>();
		
		//extract all variable-related info
		for (Method method : processedClass.getMethods()) {
			boolean selfCompatible = true;
			String methodName = method.getName();
			
			//first the reads
			Reads read = method.getAnnotation(Reads.class);
			if (read!=null && read.value().length>0) {
				for (String var : read.value()) {
					
					if (!classHasVariable(var)) {
						addError(LOC_METHOD, methodName, REF_VARIABLE, var);
					}
					
					if (reads.get(methodName)==null) {
						reads.put(methodName, new HashSet<String>());
					}
					reads.get(methodName).add(var);
					
					if (readVars.get(var)==null) {
						readVars.put(var, new HashSet<String>());
					}
					readVars.get(var).add(methodName);
				}
			}
			
			//read the modifies annotations
			Modifies modify = method.getAnnotation(Modifies.class);
			if (modify!=null && modify.value().length>0) {
				//if the method modifies something it is not selfCompatible; 
				//[!] unless defined otherwise with compatibleWith annotation
				selfCompatible = false;
				
				for (String var : modify.value()) {
					if (!classHasVariable(var)) {
						addError(LOC_METHOD, methodName, REF_VARIABLE, var);
					}
					
					if (modifs.get(methodName)==null) {
						modifs.put(methodName, new HashSet<String>());
					}
					modifs.get(methodName).add(var);
					
					if (modifVars.get(var)==null) {
						modifVars.put(var, new HashSet<String>());
					}
					modifVars.get(var).add(methodName);
				}
				
			}
			
			// if no annotations, get out!
			if (read==null && modify==null) {
				break;
			}
			
			//create a group for the method if needed, or extend the already existing one
			MethodGroup newGroup;
			if (methods.containsKey(methodName) && methods.get(methodName).name.startsWith(AD_HOC_GROUP)){
				//if the method already has an "explicit" group, modify it
				newGroup = methods.get(methodName);
			} else {
				newGroup = new MethodGroup(groups.get(method), AD_HOC_GROUP+methodName, selfCompatible);
				methods.put(methodName, newGroup);
				groups.put(newGroup.name, newGroup);
			}
			
		}
		
		//go through the annotated methods
		HashSet<String> anntMethods = new HashSet<String>();
		anntMethods.addAll(modifs.keySet());
		anntMethods.addAll(reads.keySet());
		
		for (String method : anntMethods) {
			for (String other : anntMethods) {
				//flag for compatibility.
				//A is compatible with B if
				// - A does not modify what B modifies
				// - B does not read what A modifies 
				// - A does not read what B modifies 
				boolean areOk = true;
				if (modifs.containsKey(method)) {
					for (String mVar : modifs.get(method)) {
						if ((modifVars.containsKey(mVar) && modifVars.get(mVar).contains(other)) 
								|| (readVars.containsKey(mVar) && readVars.get(mVar).contains(other))) {
							areOk=false;
							break;
						}
					}
				}
				if (areOk && reads.containsKey(method)) {
					for (String rVar : reads.get(method)) {
						if (modifVars.containsKey(rVar) && modifVars.get(rVar).contains(other)) {
							areOk=false;
							break;
						}
					}
				}
				
				if (areOk) {
					methods.get(method).addCompatibleWith(methods.get(other));
				}
			}
		}
	}*/

	/**
	 * Reads and processes the following method-level annotations:
	 * <ul>
	 *  <li>{@link MemberOf} -- the group the method belongs to</li>
	 *  <li>{@link Compatible} -- the additional methods this method is compatible with</li>
	 * </ul>
	 */
	protected void processAnnotationsAtMethods() {
		HashMap<String, HashSet<String>> compMap = new HashMap<String, HashSet<String>>();
		
		//go through each public method of a class
		for (Method method : processedClass.getMethods()) {
			
			//check what group is it part of
			MemberOf group = method.getAnnotation(MemberOf.class);
			if (group!=null) {
				MethodGroup mg = groups.get(group.value());
				methods.put(method.toString(), mg);
				
				if (mg==null) {
					addError(LOC_METHOD, method.toString(), UNDEF_GROUP, group.value());
				}
			}
			
			/*//other compatible declarations -- put them into a map
			Compatible comp = method.getAnnotation(Compatible.class);
			if (comp!=null) {
				HashSet<String> comps = new HashSet<String>();
				for (String other : comp.value()) {
					comps.add(other);
					
					if (!classHasMethod(other)) {
						addError(LOC_METHOD, method.toString(), UNDEF_GROUP, other);
					}
				}
				compMap.put(method.toString(), comps);
			}*/
		}
		
/*		//go through methods that declared compatibilities
		for (String method : compMap.keySet()) {
			boolean selfCompatible = compMap.get(method).contains(method) || (groups.get(method)!=null ? groups.get(method).isSelfCompatible() : false);
			
			//create a group for this method -- maybe extend its already existing group
			MethodGroup newGroup;
			if (methods.containsKey(method) && methods.get(method).name.startsWith(AD_HOC_GROUP)){
				newGroup = methods.get(method);
			} else {
				newGroup = new MethodGroup(groups.get(method), AD_HOC_GROUP+method, selfCompatible);
				methods.put(method, newGroup);
				groups.put(newGroup.name, newGroup);
			}
		}
		
		//set compatibilities based on the method-level compatible annotations
		for (String method : compMap.keySet()) {
			for (String other : compMap.get(method)) {
				if (compMap.containsKey(other) && compMap.get(other).contains(method)) {
					methods.get(method).addCompatibleWith(methods.get(other));
					methods.get(other).addCompatibleWith(methods.get(method));
				} else {
					addError(LOC_METHOD, method, UNDEF_METHOD, other);
				}
			}
		}*/
	}

	/**
	 * Adds an error to the error map (which keep
	 * @param locationType
	 * @param location
	 * @param problemType
	 * @param problemName
	 */
	private void addError(String locationType, String location, String problemType, String problemName) {
		String msg;
		
		if (!locationType.equals(LOC_CLASS)) {
			msg = "In '"+processedClass.getName()+"' "+locationType+" '"+location+"' "+problemType+" '"+problemName+"'";
			logger.error(msg);
			errorMessages.add(msg);
		} else {
			msg = "In '"+location+"' "+problemType+" '"+problemName+"'";
			logger.error(msg);
			errorMessages.add(msg);
		}
	}
	
	/**
	 * Returns the list of error messages thrown while processing annotations.
	 * @return
	 */
	public Collection<String> getErrorMessages(){
		return errorMessages;
	}
	
	/**
	 * Returns true if the annotations contain references (group names, method names)
	 * that are not defined in the class, or if some groups were redefined.
	 * @return
	 */
	public boolean hasErrors(){
		return errorMessages.size()>0;
	}
	
	/**
	 * Returns a map that maps the group names to the method groups.
	 * @return
	 */
	public Map<String, MethodGroup> getMethodGroups() {
		return groups;
	}

	/**
	 * Returns a map that pairs each method name with a method group.
	 * @return
	 */
	public Map<String, MethodGroup> getMethodMemberships() {
		return methods;
	}
	
	/*
	 * Returns true if the processed class has a variable named like the parameter.
	 * @param ref variable name
	 * @return
	 */
	/*private boolean classHasVariable(String what) {
		if (classVariables==null) {
			classVariables = new HashSet<String>();
			Field[] vars = processedClass.getDeclaredFields();
			for (int i=0; i<vars.length; i++) {
				classVariables.add(vars[i].getName());
			}
		}
		return classVariables.contains(what); 
	}*/
	
	private boolean classHasMethod(String what) {
		if (classMethods==null) {
			classMethods = new HashSet<String>();
			Method[] meths = processedClass.getMethods();
			for (int i=0; i<meths.length; i++) {
				classMethods.add(meths[i].getName());
			}
		}
		return classMethods.contains(what); 
	}

}
