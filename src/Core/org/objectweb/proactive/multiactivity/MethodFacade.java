package org.objectweb.proactive.multiactivity;

import java.util.List;

/**
 * This interface represents a method call, with possibilities to query the compatibility 
 * relationships with other methods.
 * @author Izso
 *
 */
public interface MethodFacade {
	
	public String getName();
	
	public List<String> getCompatibleNames();
	
	public boolean isCompatibleWithName(List<String> methodList);
	public boolean isCompatibleWith(List<MethodFacade> methodList);
	
	public boolean isCompatibleWithName(String methodName);
	public boolean isCompatibleWith(MethodFacade method);

}
