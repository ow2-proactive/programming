/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.api;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.broadcast.BroadcastDisabledException;
import org.objectweb.proactive.core.runtime.broadcast.LocalBTCallback;
import org.objectweb.proactive.core.runtime.broadcast.RTBroadcaster;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;


public class PARuntime {
    /**
     * Discover urls of runtimes that are on the same multicast group
     * A multicast group is defined using the
     * {@link CentralPAPropertyRepository#PA_RUNTIME_BROADCAST_ADDRESS} and
     * {@link CentralPAPropertyRepository#PA_RUNTIME_BROADCAST_PORT}.
     * @return an array of uris of the ProActive Runtimes discovered locally (via broadcast)
     * @throws BroadcastDisabledException thrown if the broadcast feature is disabled
     * @throws IOException thrown when the sending of the discovery request has failed
     *
    */
    protected synchronized static URI[] findRuntimesURI() throws BroadcastDisabledException, IOException {

        RTBroadcaster rtBroadcaster = RTBroadcaster.getInstance();
        if (rtBroadcaster != null) {
            LocalBTCallback lbtc = rtBroadcaster.getLocalBTCallback();

            lbtc.clear();

            rtBroadcaster.sendDiscover();

            new Sleeper(1500, ProActiveLogger.getLogger(Loggers.SLEEPER)).sleep();

            URI[] uris = (URI[]) lbtc.getKnowRuntimes().toArray(new URI[] {}).clone();

            return uris;
        } else {
            return new URI[0];
        }
    }

    /**
     * Discover runtimes that are on the same multicast group
     * A multicast group is defined using the
     * {@link CentralPAPropertyRepository#PA_RUNTIME_BROADCAST_ADDRESS} and
     * {@link CentralPAPropertyRepository#PA_RUNTIME_BROADCAST_PORT}.
     * @return a set contains ProActive Runtimes discovered locally (via broadcast)
     * @throws BroadcastDisabledException  thrown if the broadcast feature is disabled
     * @throws IOException thrown when the sending of the discovery request has failed
     *
     */
    public static Set<ProActiveRuntime> findRuntimes() throws BroadcastDisabledException, IOException {

        URI[] uris = findRuntimesURI();

        Set<ProActiveRuntime> paRTs = new HashSet<ProActiveRuntime>(uris.length);

        // add the ProActive Runtimes received from the findRuntimesURI() 
        // to the set while ignoring the failing ones
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

}
