package org.objectweb.proactive.extensions.processbuilder;

import java.util.List;


/**
 * This interface represents an abstract set of processor cores, which can be
 * enabled or disabled for process-to-core binding.
 * <p>
 * Default state for binding descriptors is to enable binding to all cores.
 * </p>
 * 
 * @author Zsolt Istvan
 * 
 */
public interface CoreBindingDescriptor {

    /**
     * Returns the number of cores that can be used for binding.
     * 
     * @return
     */
    public int getCoreCount();

    /**
     * Enables binding to this core.
     * 
     * @throws IndexOutOfBoundsException
     * @param coreIndex
     */
    public void setBound(int coreIndex);

    /**
     * Enables binding to the subset of cores given by start index and count.
     * 
     * @throws IndexOutOfBoundsException
     * @param fromCore
     * @param count
     */
    public void setBound(int fromCore, int count);

    /**
     * Disables binding to this core.
     * 
     * @throws IndexOutOfBoundsException
     * @param coreIndex
     */
    public void setNotBound(int coreIndex);

    /**
     * Disables binding to this subset of cores (given by start index and
     * count).
     * 
     * @throws IndexOutOfBoundsException
     * @param fromCore
     * @param count
     */
    public void setNotBound(int fromCore, int count);

    /**
     * Returns true if the core is enabled for binding and false if not.
     * 
     * @throws IndexOutOfBoundsException
     * @param coreIndex
     * @return
     */
    public boolean isBound(int coreIndex);

    /**
     * Returns true if <b>all</b> cores in the subset (defined by start index
     * and count) are enabled for binding.
     * 
     * @throws IndexOutOfBoundsException
     * @param fromCore
     * @param count
     * @return
     */
    public boolean areAllBound(int fromCore, int count);

    /**
     * Returns a list representation of the bound cores.
     * 
     * @return
     */
    public List<Integer> listBoundCores();

}
