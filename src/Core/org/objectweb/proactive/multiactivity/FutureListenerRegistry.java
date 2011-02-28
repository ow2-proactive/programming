package org.objectweb.proactive.multiactivity;

import java.util.HashMap;

import org.objectweb.proactive.core.UniqueID;

public class FutureListenerRegistry {
    private static HashMap<UniqueID, FutureListener> registry = new HashMap<UniqueID, FutureListener>();
    
    protected static void put(UniqueID id, FutureListener fl) {
        registry.put(id, fl);
    }
    
    public static FutureListener get(UniqueID id) {
        return registry.get(id);
    }
    

}
