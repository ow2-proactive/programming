package org.objectweb.proactive.multiactivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.annotation.multiactivity.Modifies;
import org.objectweb.proactive.annotation.multiactivity.Reads;

public class MultiActiveAnnotationProcessor {

	/**
	 * Information contained in the annotations is read and stored in this structure.
	 */
	private Map<String, AnnotatedMethod> methodInfos = new HashMap<String, AnnotatedMethod>();
	
	private Map<String, MethodGroup> methodGroups = new HashMap<String, MethodGroup>();
	
	private Map<String, Set<String>> compatibilityMap;
	
	private Map<String, List<String>> invalidReferences;
	
	private Class<?> thisClass;
	
	public MultiActiveAnnotationProcessor(Class<?> toWorkWith){
		thisClass = toWorkWith;
		readGroupInfos();
		readMethodInfos();
		generateCompatibilityMap();
	}

	/**
	 * This method will iterate through all methods from the underlying class, and
	 * create a descriptor object containing all annotations extracted. This object
	 * is put into the {@link #methodInfos} structure.
	 */
	private void readMethodInfos() {
		for (Method d : thisClass.getMethods()) {
			try {
				AnnotatedMethod am = new AnnotatedMethod();
				String methodName = d.getName();
				
				Compatible cw = d.getAnnotation(Compatible.class);
				if (cw!=null) {
					am.setCompatibleWith(cw);
				}
				
				Modifies mo = d.getAnnotation(Modifies.class);
				if (mo!=null) {
					am.setModifies(mo);
				}
				
				Reads re = d.getAnnotation(Reads.class);
				if (re!=null) {
					am.setReads(re);
				}
				
				MemberOf mof = d.getAnnotation(MemberOf.class);
				if (mof!=null) {
					for (String group : mof.value()) {
						am.addGroup(group);
					}
				}

				if (methodInfos.get(methodName)==null) {
					methodInfos.put(methodName, am);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		
		Iterator<String> i = methodInfos.keySet().iterator();
		while (i.hasNext()) {
			if (methodInfos.get(i.next()).isEmpty()) {
				i.remove();
			}
		}
		
	}
	
	/**
	 * This method will process the defined groups and compatibility rules.
	 * These are to be found at the class definition, and their processed versions
	 * are put into the {@link #methodGroups} structure.
	 */
	private void readGroupInfos(){
		//if there are any groups defined
		if (thisClass.getAnnotation(DefineGroups.class)!=null){
			DefineGroups defg = thisClass.getAnnotation(DefineGroups.class);
			
			//create the descriptor
			for (Group g : defg.value()) {
				MethodGroup mg = new MethodGroup();
				mg.setSelfCompatible(g.selfCompatible());
				methodGroups.put(g.name(), mg);
			}
			
			//if there are rules defined
			if (thisClass.getAnnotation(DefineRules.class)!=null) {
				DefineRules defr = thisClass.getAnnotation(DefineRules.class);
				
				//iterate through the rules and
				//add the compatible groups to each other's compatibility list
				for (Compatible c : defr.value()) {
					for (String name1 : c.value()) {
						for (String name2 : c.value()) {
							if (name1!=name2) {
								//if one of the method names is wrong, we fail silently
								//otherwise a wrongly written annotation would not make
								//execution possible... :(
								if (methodGroups.get(name1)!=null && methodGroups.get(name2)!=null) {
									methodGroups.get(name1).addCompatibleGroup(name2);
									methodGroups.get(name2).addCompatibleGroup(name1);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Creates the compatibility graph based on the annotations.
	 * @return
	 */
	private void generateCompatibilityMap() {
		Map<String, Set<String>> graph  = new HashMap<String, Set<String>>();
		//for all methods
		for (String method : methodInfos.keySet()) {
			graph.put(method, new HashSet<String>());
			
			String[] compList = methodInfos.get(method).getCompatibleWith();
			//if the user set some methods as compatible, put them into the neighbor list
			if (compList!=null) {
				for (String cm : compList) {
					if (!cm.equals(AnnotatedMethod.ALL)) {
						graph.get(method).add(cm);
					} else {
						graph.get(method).addAll(graph.keySet());
					}
				}
			}
			
			List<String> groups = methodInfos.get(method).getGroups();
			for (String group : groups) {
				MethodGroup mg = methodGroups.get(group);
				if (mg!=null) {
					mg.addMember(method);
				}
			}
		}
		
		//go through the groups
		for (String group : methodGroups.keySet()) {
			
			//if a group is self-compatible, add the members to each other's list
			if (methodGroups.get(group).isSelfCompatible()) {
				for (String method : methodGroups.get(group).getMembers()) {
					if (graph.get(method)==null) {
						graph.put(method, new HashSet<String>());
					}
					graph.get(method).addAll(methodGroups.get(group).getMembers());
				}
			}
			
			//now add the members of compatible groups to each other's member's groups
			for (String compGroup : methodGroups.get(group).getCompatibleGroups()) {
				for (String method : methodGroups.get(group).getMembers()) {
					if (graph.get(method)==null) {
						graph.put(method, new HashSet<String>());
					}
					graph.get(method).addAll(methodGroups.get(compGroup).getMembers());
				}
			}
		}
		
		
		//check for bidirectionality of relations. if a compatibility
		//appears to be defined only in one direction, it is deleted
		for (String method : graph.keySet()) {
			Iterator<String> sit = graph.get(method).iterator();
			while (sit.hasNext()) {
				String other = sit.next();
				if (graph.get(other)==null || !graph.get(other).contains(method)) {
					sit.remove();
				}
			}
		}
		
		//process the read-write stuff
		for (String method : methodInfos.keySet()){
			for (String other : methodInfos.keySet()) {				
				Boolean areOk = doNotInterfere(method, other);
				if (Boolean.TRUE.equals(areOk)) {
					graph.get(method).add(other);
					graph.get(other).add(method);
				} else if (Boolean.FALSE.equals(areOk)) {
					graph.get(method).remove(other);
					graph.get(other).remove(method);
				} // else - if null - leave like it is
				//System.out.println("Testing-["+method+"]-["+other+"]->"+areOk);
			}
		}
		
		//to save space, all no-neighbor methods are removed from the graph
		Iterator<String> sit = graph.keySet().iterator();
		while (sit.hasNext()) {
			if (graph.get(sit.next()).size()==0) {
				sit.remove();
			}
		}
		
		compatibilityMap = graph;
	}
	
	public MultiActiveCompatibilityMap getCompatibilityMap(){
		return new MultiActiveCompatibilityMap(compatibilityMap);
	} 
	
	public Map<String, List<String>> getInvalidReferences(){
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
	
	/**
	 * Checks whether the modified/read resources are disjoint between two methods.
	 * @param m1 first method
	 * @param m2 second method
	 * @return 
	 * <ul>
	 *  <li>true - the two methods are compatible</li>
	 *  <li>false - the two methods concurrently access resources</li>
	 *  <li>null - the two methods concurrently access resources (one of the methods lacks annotations about resources)</li>
	 * </ul>
	 */
	private Boolean doNotInterfere(String m1, String m2) {
		AnnotatedMethod maa1 = methodInfos.get(m1);
		AnnotatedMethod maa2 = methodInfos.get(m2);
		
		if ((maa1==null || maa2==null) || (maa1.isEmpty() || maa2.isEmpty()) ||
				(maa1.getModifies()==null && maa1.getReads()==null) || 
				(maa2.getModifies()==null && maa2.getReads()==null)) {
			return null;
		}
		
		if (maa1.getModifies()!=null) {
			for (String w1 : maa1.getModifies()) {
				if (maa2.getModifies()!=null) {
					for (String w2 : maa2.getModifies()) {
						if (w1.equals(w2) || w2.equals(AnnotatedMethod.ALL)) return false;
					}
				}
				if (maa2.getReads()!=null) {
					for (String w2 : maa2.getReads()) {
						if (w1.equals(w2) || w2.equals(AnnotatedMethod.ALL)) return false;
					}
				}
			}
		}
		
		if (maa2.getModifies()!=null) {
			for (String w2 : maa2.getModifies()) {
				if (maa1.getModifies()!=null) {
					for (String w1 : maa1.getModifies()) {
						if (w1.equals(w2) || w1.equals(AnnotatedMethod.ALL)) return false;
					}
				}
				if (maa1.getReads()!=null) {
					for (String w1 : maa1.getReads()) {
						if (w1.equals(w2)|| w1.equals(AnnotatedMethod.ALL)) return false;
					}
				}
			}
		}
		
		return true;
		
	}
	
	/**
	 * Checks if a class is annotated correctly. This means that all annotations contain only names
	 * that are either variable names or method names. Groups have to define compatibility with other groups.
	 * @param c Class to be verified
	 * @return true if everything is ok
	 */
	public static Boolean areAnnotationsCorrect(Class c) {
		return (new MultiActiveAnnotationProcessor(c)).areAnnotationsCorrect();
	}
	
	/**
	 * Checks if a class is annotated correctly, and returns a map of incorrect annotation elements.
	 * The keys are the method names where the annotations are to be found, and the value is a list
	 * of incorrect values.
	 * <br>
	 * NOTE: if the method name is empty, then the annotation is at the class level
	 * @param c
	 * @return
	 */
	public static Map<String, List<String>> getInvalidReferences(Class c) {
		return (new MultiActiveAnnotationProcessor(c)).getInvalidReferences();
	}
	
	/**
	 * Checks if a class is annotated correctly, and if not, will print a message listing the location
	 * of incorrectly written annotations.
	 * @param c
	 */
	public static void printInvalidReferences(Class c) {
		Map<String, List<String>> invalid = (new MultiActiveAnnotationProcessor(c)).getInvalidReferences();
		for (String source : invalid.keySet()) {
			System.out.println("At "+((source.length()>0)? "method " : "the definitions ")+source+":");
			for (String ref : invalid.get(source)) {
				System.out.println(" '"+ref+"'");
			}
		}
	}
	
	/**
	 * Returns the compatibility map of a class. 
	 * <br>
	 * NOTE: the annotations are not checked for correctness, and any incorrect annotation is ignored.
	 * Use the checking methods to make sure everything will work as expected.
	 * @param c
	 * @return
	 */
	public static MultiActiveCompatibilityMap getCompatibilityMap(Class c) {
		return (new MultiActiveAnnotationProcessor(c)).getCompatibilityMap();
	}
	
	/**
	 * Descriptor for annotated methods
	 * @author Zsolt István
	 *
	 */
	protected class AnnotatedMethod {
		public static final String ALL = "*";
		private Reads readVars;
		private Modifies modifiesVars;
		private Compatible compatibleMethods;
		private List<String> memberOfGroups = new LinkedList<String>();
		
		public void setReads(Reads reads) {
			readVars = reads;
		}
		
		public String[] getReads(){
			return readVars!=null ? readVars.value() : new String[0]; 
		}
		
		public void setModifies(Modifies modifs) {
			modifiesVars = modifs;
		}
		
		public String[] getModifies(){
			return modifiesVars!=null ? modifiesVars.value() : new String[0]; 
		}
		
		public void setCompatibleWith(Compatible cw) {
			compatibleMethods = cw;
		}
		
		public String[] getCompatibleWith(){
			return (compatibleMethods==null) ? new String[0] : compatibleMethods.value();
		}
		
		public void addGroup(String group) {
			memberOfGroups.add(group);
		}
		
		public List<String> getGroups(){
			return memberOfGroups;
		}
		
		public boolean isEmpty(){
			return (modifiesVars==null && readVars==null 
					&& compatibleMethods==null && memberOfGroups.size()==0);
		}
	}
	
	/**
	 * Descriptor for a group defined at the top of the class
	 * @author Zsolt István
	 *
	 */
	protected class MethodGroup {
		private List<String> compatibleWith = new LinkedList<String>();
		private List<String> members = new LinkedList<String>();
		private boolean selfCompatible;
		
		public void addCompatibleGroup(String groupName) {
			compatibleWith.add(groupName);
		}
		
		public List<String> getCompatibleGroups(){
			return compatibleWith;
		}	
		
		public void addMember(String method) {
			members.add(method);
		}
		
		public List<String> getMembers(){
			return members;
		}

		public void setSelfCompatible(boolean selfCompatible) {
			this.selfCompatible = selfCompatible;
		}

		public boolean isSelfCompatible() {
			return selfCompatible;
		}
	}
	
}
