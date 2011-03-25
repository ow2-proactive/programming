package org.objectweb.proactive.multiactivity.execution;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.request.Request;

public interface RequestSupplier {
    
    public void finished(Request r);
    
    public Request pullRequest();

}
