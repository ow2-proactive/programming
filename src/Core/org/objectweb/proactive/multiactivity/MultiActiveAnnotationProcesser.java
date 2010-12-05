package org.objectweb.proactive.multiactivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;
import org.objectweb.proactive.annotation.multiactivity.Modifies;
import org.objectweb.proactive.annotation.multiactivity.Reads;

public class MultiActiveAnnotationProcesser {

	/**
	 * Information contained in the annotations is read and stored in this structure.
	 */
	private Map<String, MultiActiveAnnotations> methodInfo = new HashMap<String, MultiActiveAnnotations>();
	
	private Map<String, List<String>> compatibilityGraph;
	
	private Map<String, List<String>> invalidReferences;
	
	private Class<?> thisClass;
	
	public MultiActiveAnnotationProcesser(Class<?> toWorkWith){
		thisClass = toWorkWith;
		readMethodInfos();
		generateCompatibilityGraph();
	}

	/**
	 * This method will iterate through all methods from the underlying class, and
	 * create a descriptor object containing all annotations extracted. This object
	 * is put into the {@link #methodInfo} structure.
	 */
	private void readMethodInfos() {
		try {
			for (Method d : thisClass.getMethods()) {
				if (methodInfo.get(d.getName())==null) {
					methodInfo.put(d.getName(), new MultiActiveAnnotations());
				}
				CompatibleWith cw = d.getAnnotation(CompatibleWith.class);
				if (cw!=null) {
					
					methodInfo.get(d.getName()).setCompatibleWith(cw);
				}
				
				Modifies mo = d.getAnnotation(Modifies.class);
				if (mo!=null) {
					methodInfo.get(d.getName()).setModifies(mo);
				}
				
				Reads re = d.getAnnotation(Reads.class);
				if (re!=null) {
					methodInfo.get(d.getName()).setReads(re);
				}
				//System.out.println(d.getName()+" "+cw+" "+mo+" "+re);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the compatibility graph based on the annotations.
	 * @return
	 */
	private void generateCompatibilityGraph() {
		Map<String, List<String>> graph  = new HashMap<String, List<String>>();
		//for all methods
		for (String method : methodInfo.keySet()) {
			graph.put(method, new ArrayList<String>());
			
			String[] compList = methodInfo.get(method).getCompatibleWith();
			//if the user set some methods as compatible, put them into the neighbor list
			if (compList!=null) {
				for (String cm : compList) {
					if (!cm.equals(MultiActiveAnnotations.ALL)) {
						graph.get(method).add(cm);
					} else {
						graph.get(method).addAll(graph.keySet());
					}
				}
			}
		}
		
		//check for bidirectionality of relations. if a compatibleWith is only
		// expressed in one direction, it is deleted
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
		for (String method : methodInfo.keySet()){
			for (String other : methodInfo.keySet()) {				
				Boolean areOk = areDisjoint(method, other);
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
		
		//to save space, all no-neighbour methods are removed from the graph
		Iterator<String> sit = graph.keySet().iterator();
		while (sit.hasNext()) {
			if (graph.get(sit.next()).size()==0) {
				sit.remove();
			}
		}
		
		compatibilityGraph = graph;
	}
	
	public Map<String, List<String>> getCompatibilityGraph(){
		return compatibilityGraph;
	} 
	
	public Map<String, List<String>> getInvalidReferences(){
		if (invalidReferences==null) {
			invalidReferences = new HashMap<String, List<String>>();
			for (String m : methodInfo.keySet()){
				MultiActiveAnnotations ann = methodInfo.get(m);
				for (String ref : ann.getCompatibleWith()) {
					checkReferenceCorrectness(m, ref);
				}
				for (String ref : ann.getModifies()) {
					checkReferenceCorrectness(m, ref);
				}
				for (String ref : ann.getReads()) {
					checkReferenceCorrectness(m, ref);
				}
			}
		}
		return invalidReferences;
	}

	private void checkReferenceCorrectness(String sourceMethod, String reference) {
		if (!reference.startsWith("#") && !classHasPublicMethod(reference) && !classHasVariable(reference)) {
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
	 *  <li>null - impossible to decide (one of the methods lacks annotations about resources)</li>
	 * </ul>
	 */
	private Boolean areDisjoint(String m1, String m2) {
		MultiActiveAnnotations maa1 = methodInfo.get(m1);
		MultiActiveAnnotations maa2 = methodInfo.get(m2);
		
		if ((maa1==null || maa2==null) || 
				(maa1.getModifies()==null && maa1.getReads()==null) || 
				(maa2.getModifies()==null && maa2.getReads()==null)) {
			return null;
		}
		
		if (maa1.getModifies()!=null) {
			for (String w1 : maa1.getModifies()) {
				if (maa2.getModifies()!=null) {
					for (String w2 : maa2.getModifies()) {
						if (w1.equals(w2) || w2.equals(MultiActiveAnnotations.ALL)) return false;
					}
				}
				if (maa2.getReads()!=null) {
					for (String w2 : maa2.getReads()) {
						if (w1.equals(w2) || w2.equals(MultiActiveAnnotations.ALL)) return false;
					}
				}
			}
		}
		
		if (maa2.getModifies()!=null) {
			for (String w2 : maa2.getModifies()) {
				if (maa1.getModifies()!=null) {
					for (String w1 : maa1.getModifies()) {
						if (w1.equals(w2) || w1.equals(MultiActiveAnnotations.ALL)) return false;
					}
				}
				if (maa1.getReads()!=null) {
					for (String w1 : maa1.getReads()) {
						if (w1.equals(w2)|| w1.equals(MultiActiveAnnotations.ALL)) return false;
					}
				}
			}
		}
		
		return true;
		
	}
	
	
	/**
	 * Container for annotations of methods.
	 * The getter methods are simplified to return arrays of
	 * strings, thus we don't have to couple the methods in the 
	 * multi-active-service to the actual annotation classes.
	 * @author Izso
	 *
	 */
	protected class MultiActiveAnnotations {
		public static final String ALL = "*";
		private CompatibleWith compatibleWith;
		private Reads readVars;
		private Modifies modifiesVars;
		
		public MultiActiveAnnotations(){
			
		}
		
		public void setCompatibleWith(CompatibleWith annotation) {
			compatibleWith = annotation;
		}
		
		public String[] getCompatibleWith(){
			return compatibleWith!=null ? compatibleWith.value() : new String[0];
		}
		
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
	}
	
}
