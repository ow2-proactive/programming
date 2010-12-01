package org.objectweb.proactive.multiactivity;

import java.util.List;

/**
 * Interface which represents a read-only graph that shows which methods are compatible with which ones
 * @author Izso
 *
 */
public interface CompatibilityGraph {
	
	public List<MethodFacade> getNeighbours(MethodFacade method);
	public boolean areConnex(List<MethodFacade> methodList);
	public List<MethodFacade> getMaximumConnex(List<MethodFacade> methodList);

}
