package org.objectweb.proactive.multiactivity.compatibility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.core.body.request.Request;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter;

/**
 * This class represents a group of methods. A group is compatible with other groups, meaning that
 * methods belonging to these groups can run in parallel.
 * @author Zsolt Istvan
 *
 */
public class MethodGroup {

	public final String name;
	private final boolean selfCompatible;
	
	private Set<MethodGroup> compatibleWith = new HashSet<MethodGroup>();
	
	private final int hashCode;
	
	private Class parameter = null;
	private HashMap<String, Integer> methodParamPos = new HashMap<String, Integer>();
	private String internalComparator = "";
	private String externalComparator = "";
	
	/**
	 * Standard constructor of a named group.
	 * @param name A descriptive name for the group -- all group names have to be unique
	 * @param selfCompatible if the methods that are members of the group can run in parallel
	 */
	public MethodGroup(String name, boolean selfCompatible) {
		this.selfCompatible = selfCompatible;
		this.name = name;
		this.hashCode = name.hashCode();
		
		if (selfCompatible) {
			this.compatibleWith.add(this);
		}
		
    }

    public MethodGroup(String name, boolean selfCompatible, String parameter) {
        this(name, selfCompatible);
        if (parameter.length()>0) {
            try {
                this.parameter = Class.forName(parameter);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
    }
    
    public MethodGroup(String name, boolean selfCompatible, String parameter, String comparator) {
        this(name, selfCompatible, parameter);
        
        this.internalComparator = comparator;
    }
	
	/**
	 * Standard constructor of a named group.
	 * @param name A descriptive name for the group -- all group names have to be unique
	 * @param selfCompatible if the methods that are members of the group can run in parallel
	 */
	public MethodGroup(MethodGroup from, String name, boolean selfCompatible) {
		this.selfCompatible = selfCompatible;
		this.name = name;
		this.hashCode = name.hashCode();
		
		if (selfCompatible) {
			this.compatibleWith.add(this);
		}
		
		if (from!=null) {
			this.addCompatibleWith(from.compatibleWith);
			if (from.selfCompatible) {
				from.addCompatibleWith(this);
			}
		}
		
	}
	
	/**
	 * Set the set of compatible groups
	 * @param compatibleWith
	 */
	public void setCompatibleWith(Set<MethodGroup> compatibleWith) {
		this.compatibleWith = compatibleWith;
	}
	
	/**
	 * Add a group whose methods will be runnable in parallel with the methods
	 * belonging to this one
	 * @param compatibleWith the other group
	 */
	public void addCompatibleWith(MethodGroup compatibleWith) {
		this.compatibleWith.add(compatibleWith);
	}

	public void addCompatibleWith(Set<MethodGroup> compatibleSet) {
		this.compatibleWith.addAll(compatibleSet);
		
	}

	/**
	 * Returns the set of the groups whose member methods can run in parallel with the
	 * methods belonging to this group
	 * @return
	 */
	public Set<MethodGroup> getCompatibleWith() {
		return compatibleWith;
	}

	public boolean isSelfCompatible() {
		return selfCompatible;
	}
	
	public Class getParameterType(){
	    return parameter;
	}
	
	private Object mapMethodParameters(String name, Object[] params){
	    if (parameter==null) {
	        return null;
	    }
	    if (methodParamPos.get(name)==null) {
    	    for (int i=0; i<params.length; i++) {
    	        if (params[i].getClass().equals(parameter)) {
    	            methodParamPos.put(name, i);
    	            return params[i];
    	        }
    	    }
	    } else {
	        return params[methodParamPos.get(name)];
	    }
	    
	    return null;
	}
	
	public boolean isCompatible(Request request1, Request request2) {
	    if (!isSelfCompatible()) {
	        return false;
	    }
	    
	    Object param1 = mapMethodParameters(request1.getMethodName(), request1.getMethodCall().getParameters());
        Object param2 = mapMethodParameters(request2.getMethodName(), request2.getMethodCall().getParameters());
        if (param1==null || param2==null) {
            return true;
        } else if (internalComparator.equals("equals") && !param1.equals(param2)) {
            return true;
        } else if (internalComparator.equals("")) {
            return true;
        }
        
        return false;
	}
	
	public boolean isCompatible(Request r1, MethodGroup other, Request r2) {
	    if (this.equals(other)) {
	        return isCompatible(r1, r2);
	    }
	    
        if (other!=null) {
            boolean rules =  (getCompatibleWith().contains(other) || other.getCompatibleWith().contains(this));
            if (rules == true) {
                Object param1 = mapMethodParameters(r1.getMethodName(), r1.getMethodCall().getParameters());
                Object param2 = other.mapMethodParameters(r2.getMethodName(), r2.getMethodCall().getParameters());
                if ((param1==null || param2==null) || (externalComparator.equals("equals") && !param1.equals(param2))) {
                    return true;
                } else if (externalComparator.equals("")) {
                    return true;
                }
            }
        } 
        return false;
    }

	@Override
	public int hashCode() {
		return hashCode; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodGroup) {
			MethodGroup other = (MethodGroup) obj;
			return (this.name.equals(other.name));
		} 
		return false;
	}

    public void setExternalComparator(String externalComparator) {
        this.externalComparator = externalComparator;
    }
	
}
