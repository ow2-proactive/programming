package org.objectweb.proactive.multiactivity;

import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.request.Request;

public interface ThreadManager {
    
    public void submit(Request r);
    
    public int getNumberOfReady();
    public int getNumberOfActive();
    public int getNumberOfWaiting();

}
