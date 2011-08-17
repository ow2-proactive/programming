package org.objectweb.proactive.multiactivity.execution;

import java.util.HashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;

/**
 * Static class that pairs {@link Body} instances with {@link FutureWaiter}s. Since each body has only one associated service, and each service
 * can have only one internal future waiter, the mapping is one-to-one.
 * @author Zsolt Istvan
 *
 */
public class FutureWaiterRegistry {
    private static HashMap<UniqueID, FutureWaiter> registry = new HashMap<UniqueID, FutureWaiter>();
    
    /**
     * Pairs a future waiter with a body.
     * @param id the ID of the body
     * @param fl the future waiter implementation
     */
    public static void putForBody(UniqueID id, FutureWaiter fl) {
        registry.put(id, fl);
    }
    
    /**
     * Returns the future waiter associated with a body. 
     * @param id the ID of the body
     * @return the future waiter instance, or null if there is no binding
     */
    public static FutureWaiter getForBody(UniqueID id) {
        return registry.get(id);
    }
    

}
