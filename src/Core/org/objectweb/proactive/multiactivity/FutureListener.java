package org.objectweb.proactive.multiactivity;

import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureID;

public interface FutureListener {
    
    public void waitingFor(Future future);
    public void arrived(Future future);
    
}
