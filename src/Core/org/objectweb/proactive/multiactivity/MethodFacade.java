package org.objectweb.proactive.multiactivity;

/**
 * This interface represents a method call.
 * @author Izso
 *
 */
/*
 * The reason we need a wrapper for the name of the method is that the scheduler
 * will most probably have some extra information hidden in this object (like the
 * request behind the method call). 
 */
public interface MethodFacade {
	
	public String getName();

}
