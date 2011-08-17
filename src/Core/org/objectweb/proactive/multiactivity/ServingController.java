package org.objectweb.proactive.multiactivity;

/**
 * Interface for a controller object that can set the properties of geenric multi-active services.
 * @author Zsolt Istvan
 *
 */
public interface ServingController {
    
	/**
	 * Get the number of allowed concurrent servings inside the service.
	 * @return
	 */
    public int getNumberOfConcurrent();
    
    /**
     * Set the number of allowed concurrent servings inside the service.
     * @param numActive (has to be >0, otherwise nothing is changed)
     */
    public void setNumberOfConcurrent(int numActive);
    
    /**
     * Decrease with one the number of allowed concurrent servings inside the service.
     * @return the new limit
     */
    public int decrementNumberOfConcurrent();
    
    /**
     * Decrease with "dec" (>0) the number of allowed concurrent servings inside the service.
     * @return the new limit 
     */
    public int decrementNumberOfConcurrent(int dec);
    
    /**
     * Increase with one the number of allowed concurrent servings inside the service.
     * @return the new limit
     */
    public int incrementNumberOfConcurrent();
    
    /**
     * Increase with "inc" (>0) the number of allowed concurrent servings inside the service.
     * @return the new limit 
     */
    public int incrementNumberOfConcurrent(int inc);    

}
