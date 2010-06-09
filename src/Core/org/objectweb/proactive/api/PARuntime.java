package org.objectweb.proactive.api;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.broadcast.LocalBTCallback;
import org.objectweb.proactive.core.runtime.broadcast.RTBroadcaster;
import org.objectweb.proactive.core.util.Sleeper;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class PARuntime {
    /**
     * Discover urls of runtimes that are on the same multicast group
     * A multicast group is defined using the
     * {@link CentralPAPropertyRepository#PA_RUNTIME_BROADCAST_ADDRESS} and
     * {@link CentralPAPropertyRepository#PA_RUNTIME_BROADCAST_PORT}.
     * @return an array of uris of the ProActive Runtimes discovered locally (via broadcast)
     *
    */
    protected synchronized static URI[] findRuntimesURI() {

        RTBroadcaster rtBroadcaster = RTBroadcaster.getInstance();

        LocalBTCallback lbtc = rtBroadcaster.getLocalBTCallback();

        lbtc.clear();

        rtBroadcaster.sendDiscover();

        new Sleeper(1500).sleep();

        URI[] uris = (URI[]) lbtc.getKnowRuntimes().toArray(new URI[] {}).clone();

        return uris;
    }

    /**
     * Discover runtimes that are on the same multicast group
     * A multicast group is defined using the
     * {@link CentralPAPropertyRepository#PA_RUNTIME_BROADCAST_ADDRESS} and
     * {@link CentralPAPropertyRepository#PA_RUNTIME_BROADCAST_PORT}.
     * @return a set contains ProActive Runtimes discovered locally (via broadcast)
     *
     */
    public static Set<ProActiveRuntime> findRuntimes() {

        URI[] uris = findRuntimesURI();

        Set<ProActiveRuntime> paRTs = new HashSet<ProActiveRuntime>(uris.length);

        // Search all ProActive Runtimes and ignore failing ones
        for (URI url : uris) {
            try {
                ProActiveRuntime pa = RuntimeFactory.getRuntime(url.toString());
                paRTs.add(pa);
            } catch (Exception e) {
                ProActiveLogger.logEatedException(ProActiveLogger.getLogger(Loggers.RUNTIME), e);
            }
        }

        return paRTs;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(findRuntimes().toArray()));
        System.exit(0);
    }
}
