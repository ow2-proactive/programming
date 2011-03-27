package org.objectweb.proactive.multiactivity;

public interface ServingController {
    
    public int getNumberOfConcurrent();
    
    public void setNumberOfConcurrent(int numActive);
    
    public int decrementNumberOfConcurrent();
    
    public int decrementNumberOfConcurrent(int dec);
    
    public int incrementNumberOfConcurrent();
    
    public int incrementNumberOfConcurrent(int inc);
    
    //TODO maybe add the others also

}
