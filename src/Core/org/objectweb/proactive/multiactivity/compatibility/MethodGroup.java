package org.objectweb.proactive.multiactivity.compatibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;

/**
 * This class represents a group of methods. 
 * A group can be compatible with other groups, meaning that methods belonging to these groups can run in parallel.
 * @author  Zsolt Istvan
 */
public class MethodGroup {

	public final String name;
	private final boolean selfCompatible;
	
	private Set<MethodGroup> compatibleWith = new HashSet<MethodGroup>();
	
	private final int hashCode;
	
	private Class<?> parameter = null;
	private HashMap<String, Integer> parameterPosition = new HashMap<String, Integer>();
	/**
	 * Hashmap that contains the name of an other group and the comparator name that has to be used
	 * when comparing parameters of methods from his group with that other group.
	 * A comparator can be either like "equals" (member function of parameter) or like
	 * "org.foo.someclass.someFunction" that is a static method in a class, or "this.someMethod" 
	 * in which case it is a method of the active object.
	 */
	private HashMap<String, String> comparators = new HashMap<String, String>();
	
	/**
	 * Cache for the actual Method objects found for the comparators functions.
	 */
	private HashMap<String, Method> comparatorCache = new HashMap<String, Method>();
	
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

	/**
	 * Constructor of a named group with common parameter.
	 * @param name A descriptive name for the group -- all group names have to be unique
	 * @param selfCompatible if the methods that are members of the group can run in parallel
	 * @param parameter -- the common argument to all methods inside the group (type)
	 */
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
    
    /**
	 * Constructor of a named group with common parameter and a comparator function for checking in-group compatibility.
	 * @param name A descriptive name for the group -- all group names have to be unique
	 * @param selfCompatible if the methods that are members of the group can run in parallel
	 * @param parameter -- the common argument to all methods inside the group (type)
	 * @param comparator -- function to use to condition compatibility inside the group.
	 */
    public MethodGroup(String name, boolean selfCompatible, String parameter, String comparator) {
        this(name, selfCompatible, parameter);
        
        if (!comparator.equals("")) {
            this.comparators.put(name, comparator);
        }
    }
	
    /*
     * Constructor which inherits all information 
     * @param from
     * @param name
     * @param selfCompatible
     *//*
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
		
	}*/
	
	/**
	 * Set the set of compatible groups with this group
	 * @param  compatibleWith
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

	/**
	 * Add a set of compatible groups to the compatible ones
	 * @param  compatibleSet
	 */
	public void addCompatibleWith(Set<MethodGroup> compatibleSet) {
		this.compatibleWith.addAll(compatibleSet);
		
	}

	/**
	 * Returns the set of the groups whose member methods can run in parallel with the methods belonging to this group (are compatible)
	 * @return
	 */
	public Set<MethodGroup> getCompatibleWith() {
		return compatibleWith;
	}

	/**
	 * Returns true if the two groups are compatible
	 * @param otherGroup
	 * @return
	 */
	public boolean isCompatibleWith(MethodGroup otherGroup) {
        return compatibleWith.contains(otherGroup);
    }

	/**
	 * Returns true if the group is selfCompatible (methods belonging to this group
	 * may run in parallel -- if condition/comparator holds)
	 * @return
	 */
    public boolean isSelfCompatible() {
		return selfCompatible;
	}
	
    /**
     * Parameter that is common to the methods inside the group.
     * @return
     */
	public Class<?> getGroupParameter(){
	    return parameter;
	}
	
	/**
	 * Returns the argument from the argument list of the method call that matches the 
	 * group parameter in type. If several arguments of the same type exist, the leftmost is chosen.
	 * @param r
	 * @return
	 */
	private Object getGroupParameterFor(Request r){
		String name = getNameOf(r);
		Object[] params = r.getMethodCall().getParameters();
		
	    if (parameter==null) {
	        return null;
	    }
	    if (parameterPosition.get(name)!=null) {
	        return params[parameterPosition.get(name)];
	    } else {
	        for (int i=0; i<params.length; i++) {
                if (params[i].getClass().equals(parameter)) {
                    parameterPosition.put(name, i);
                    return params[i];
                }
            }
	    }
	    
	    return null;
	}
	
	/**
	 * Checks if two requests belonging to this group are compatible or not
	 * @param request1
	 * @param request2
	 * @return
	 */
	public boolean isCompatible(Request request1, Request request2) {
	    if (!isSelfCompatible()) {
	        return false;
	    }
	    if (comparators.get(name)!=null) {
	        Object param1 = getGroupParameterFor(request1);
	        Object param2 = getGroupParameterFor(request2);
	        return evaluateComparator(param1, param2, comparators.get(name));
	    } else {
	        return true;
	    }
	}
	
	/**
	 * Compares a request from this group and a request from an other group.
	 * @param r1 Request belonging to this group
	 * @param other Other group
	 * @param r2 Request belonging to the other group
	 * @return
	 */
	public boolean isCompatible(Request r1, MethodGroup other, Request r2) {
	    if (this.equals(other)) {
	        return isCompatible(r1, r2);
	    }
	    
        if (other!=null) {
            boolean rules =  (getCompatibleWith().contains(other) || other.getCompatibleWith().contains(this));
            if (rules == true && comparators.containsKey(other.name)) {
                Object param1 = getGroupParameterFor(r1);
                Object param2 = other.getGroupParameterFor(r2);
                if (comparators.get(other.name)!=null) { 
                	return evaluateComparator(param1, param2, comparators.get(other.name));
                }
            }
        } 
        return false;
    }

	/**
	 * Performs the comparison between two parameters. 
	 * The comparison is done by running a given method of either of the parameters, and
	 * negating the result.
	 * @param param1 
	 * @param param2
	 * @param comparator name of the method to run (this can be in either one of the parameters -- if their classes differ)
	 * @return True if the parameters are different and false if they are the same based on the method given as argument
	 */
    private Boolean evaluateComparator(Object param1, Object param2, String comparator) {
    	boolean affirmative = !comparator.startsWith("!");
    	String cmp = comparator.replace("!", "");
    	
    	if (comparator.length()==0) {
    		return false;
    	}
           
    	//most common comparator: equals -- we will not do reflection for this
        if (cmp.equals("equals")) {
        	
        	if (param1!=null && param2!=null) {
        		boolean result = param1.equals(param2);
        		return  affirmative ? result : !result;
        	} else {
        		return affirmative ? false : true;
        	}
            
        } else if (!cmp.contains(".")) {
            //execute a comparator method that is defined in either of the parameter's classes
            //has to return a boolean or integer as a result.

        	boolean res = evaluateParameterMethod(param1, param2, cmp);        	
            return affirmative ? res : !res;
            
        } else if (cmp.startsWith("this.")) {
            //execute a method of the underlying active object
            //it has to return a boolean or integer as a result.

        	boolean res = evaluateLocalMethod(param1, param2, cmp);
        	return affirmative ? res : !res;
        } else {
        	//execute a static method of an arbitrary class
            //it has to return a boolean or integer as a result.
        	
        	boolean res = evaluateStaticMethod(param1, param2, cmp);
        	return affirmative ? res : !res;
        }
    }

    /**
	 * Compares the two objects with a comparator that is a member function of one of these objects.
	 * 
	 * @param param1 
	 * @param param2
	 * @param comparator name of the method to be used for comparison
	 * @return the result of "param1.<i>comparator</i>(param2)" or "param2.<i>comparator</i>(param1)" or false if both parameters are <b>null</b>, or the method does not exist
	 */
	private boolean evaluateParameterMethod(Object param1, Object param2, String comparator) {
		if (param1!=null) {
		    try {
		        Class<?> clazz = param1.getClass();

		        Object res = invokeMethod(clazz, comparator, param2, null, param1, false);
		        if (res instanceof Boolean) {
		            return ((Boolean) res);
		        } else {
		            return ((Integer) res) == 0;
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		
		if (param2!=null) {
		    try {
		        Class<?> clazz = param2.getClass();
		        
		        Object res = invokeMethod(clazz, comparator, param1, null, param2, false);
		        if (res instanceof Boolean) {
		            return ((Boolean) res);
		        } else {
		            return ((Integer) res) == 0;
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		
	    return false;
	}

	/**
	 * Comapres two parameters with the help of a method that is member of the active object. 
	 * @param param1
	 * @param param2
	 * @param comparator name of the method to use for comparison
	 * @return the result of "<i>comparator</i>(param1, param2)", or "<i>comparator</i>(param1)" if param2 is <b>null</b>, or "<i>comparator</i>(param2)" if param1 is <b>null</b>, 
	 * or "<i>comparator</i>()" if both of them are <b>null</b>. If the function does not exist false is returned. 
	 */
	private Boolean evaluateLocalMethod(Object param1, Object param2, String comparator) {
		 try {
	            Class<?> clazz = PAActiveObject.getBodyOnThis().getReifiedObject().getClass();
	            String methodName = comparator.replace("this.", "");
	            
	            Object res = invokeMethod(clazz, methodName, param1, param2, PAActiveObject.getBodyOnThis().getReifiedObject(), true);
	            
	            if (res instanceof Boolean) {
	                return ((Boolean) res);
	            } else {
	                return ((Integer) res) == 0;
	            }
		 } catch (Exception e) {
			e.printStackTrace();
		 }
		 
		 return false;
	}
	
	/**
	 * Compares the two objects given as parameters with a comparator that 
	 * is a static public method of a class.
	 *  To speed up comparison a cache is used for holding the Method
	 * object of the canonical-name-identified comparator function.
	 * @param param1
	 * @param param2
	 * @param comparator
	 * @return  the result of "<i>comparator</i>(param1, param2)", or "<i>comparator</i>(param1)" if param2 is <b>null</b>, or "<i>comparator</i>(param2)" if param1 is <b>null</b>, 
	 * or "<i>comparator</i>()" if both of them are <b>null</b>. If the function does not exist false is returned. 
	 */
	private boolean evaluateStaticMethod(Object param1, Object param2, String comparator) {
	    try {
	        String clazzName = comparator.substring(0, comparator.lastIndexOf('.'));
	        Class<?> clazz = Class.forName(clazzName);
	        String methodName = comparator.substring(comparator.lastIndexOf('.') + 1, comparator.length());
	            
	        Object res = invokeMethod(clazz, methodName, param1, param2, null, false);
	            
	        if (res instanceof Boolean) {
	            return ((Boolean) res);
	        } else {
	            return ((Integer) res) == 0;
	        }
		 } catch (Exception e) {
			e.printStackTrace();
		 }
		 
		 return false;
	}

	/**
	 * Invokes the given method with the arguments. In case the method is overloaded, the version
	 * which matches the parameter types (and number) is chosen.
	 * 
	 * <br>
	 * A cache is used to reduce the number of reflection calls. 
	 * It stores method instances for a given class and parameter types.
	 * @param clazz the class in which to look for this method
	 * @param method the name of the method to invoke
	 * @param param1 one parameter to the method (may be null)
	 * @param param2 other parameter to the method (may be null)
	 * @param target the object upon which to invoke the method (may be null for static methods)
	 * @param enablePrivate if set to true the method will be executed even if it is private. 
	 * Otherwise only public methods are considered
	 * @return Result of the method call
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private Object invokeMethod(Class clazz, String method, Object param1, Object param2, Object target, boolean enablePrivate) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object p1 = param1!=null && param2!=null && param1.getClass().toString().compareTo(param2.getClass().toString())<=0 ? param1 : param2;
		Object p2 = p1==param1 ? param2 : param1;
		
		String cachedName = clazz.toString() +"."+method+
							"("+(p1!=null ? p1.getClass().toString() : "")+
							(p1!=null && p2!=null ? "," : "")+
							(p2!=null ? p2.getClass().toString() : "")+")";
		
		if (!comparatorCache.containsKey(cachedName)) {
			Method[] meths = enablePrivate ? clazz.getDeclaredMethods() : clazz.getMethods();
			if (p1!=null && p2!=null) {
				for (Method cmp : meths) {
		            if (cmp.getName().equals(method)) {
		                if (cmp.getParameterTypes().length==2) {
		                	
		                	if (cmp.getParameterTypes()[0].isAssignableFrom(p1.getClass()) && 
		                		cmp.getParameterTypes()[1].isAssignableFrom(p2.getClass())) {
		                		
		                		comparatorCache.put(cachedName, cmp);
		                		break;
		                	}
		                	if (cmp.getParameterTypes()[1].isAssignableFrom(p1.getClass()) && 
		                		cmp.getParameterTypes()[0].isAssignableFrom(p2.getClass())) {
	                					  
		                		comparatorCache.put(cachedName, cmp);
		                		break;
		                	}
		                }
		            }
		        }
			} else if (p1!=null) {
				for (Method cmp : meths) {
		            if (cmp.getName().equals(method)) {
		                if (cmp.getParameterTypes().length==1) {
		                	
		                	if (cmp.getParameterTypes()[0].isAssignableFrom(p1.getClass())){
		                		comparatorCache.put(cachedName, cmp);
		                		break;
		                	}
		                }
		            }
		        }
				
			} else if (p2!=null) {
				for (Method cmp : meths) {
		            if (cmp.getName().equals(method)) {
		                if (cmp.getParameterTypes().length==1) {
		                	
		                	if (cmp.getParameterTypes()[0].isAssignableFrom(p2.getClass())){
		                		comparatorCache.put(cachedName, cmp);
		                		break;
		                	}
		                }
		            }
				}
			} else {
				for (Method cmp : meths) {
		            if (cmp.getName().equals(method)) {
		                if (cmp.getParameterTypes().length==0) {
		                	
		                		comparatorCache.put(cachedName, cmp);
		                		break;
		                }
		            }
		        }
			}
		}
		
		
		Method m = comparatorCache.get(cachedName);
		if (enablePrivate) {
			m.setAccessible(true);
		}
		
		if (m==null) {
			throw new NullPointerException("Method "+cachedName+" was not found!");
		}
			
		if (p1!=null && p2!=null) {
			return (m.getParameterTypes()[0].isAssignableFrom(p1.getClass())) ? m.invoke(target, p1, p2) : m.invoke(target, p2, p1);
		} else if (p1!=null) {
			return m.invoke(target, p1);
		} else if (p2!=null) {
			return m.invoke(target, p2);
		} else {
			return m.invoke(target);
		}
			
			
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

	/**
	 * Sets the comparator function to be used when deciding compatibility with the given group
	 * @param group
	 * @param comp
	 */
    public void setComparatorFor(String group, String comp) {
        if (!comp.equals("")) {
            this.comparators.put(group, comp);
        }
    }
    
    /**
     * Checks if there is a comparator function set for conditioning the relation with the other group
     * @param other
     * @return
     */
    public boolean isComparatorDefinedFor(MethodGroup other) {
        return (other!=null) ? comparators.containsKey(other.name) : false;
    }
    
    @Override
    public String toString() {
        return "Group " + this.name+" (compatible with "+compatibleWith+")";
    }
    
    /**
     * Standard way of getting the name of the method underlying the request. Use this everywhere.
     * @param r
     * @return
     */
    public static String getNameOf(Request r) {
    	return r.getMethodCall().getReifiedMethod().toString();
    }
	
}
