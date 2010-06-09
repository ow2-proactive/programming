package org.objectweb.proactive.core.runtime.broadcast;

import java.net.URI;
import java.util.Set;


public interface LocalBTCallback extends BTCallback {

    public Set<URI> getKnowRuntimes();

    public void clear();
}
