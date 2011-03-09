package org.objectweb.proactive.multiactivity.execution;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.request.Request;

public interface RequestSupplier {
    
    public Body getServingBody();
    
    public void finished(Request r);

}
