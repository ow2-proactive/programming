/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.api;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.exceptions.ProActiveBadConfigurationException;
import org.objectweb.proactive.core.gc.HalfBodies;
import org.objectweb.proactive.core.util.ProActiveInet;


/**
 * This class provides helpers for both explicit and implicit application termination.
 *
 * @author The ProActive Team
 * @since ProActive 3.9 (December 2007)
 */
@PublicAPI
public class PALifeCycle {

    /**
     * Inform the ProActive DGC that all non active threads will not use
     * anymore their references to active objects. This is needed when the
     * local GC does not reclaim stubs quickly enough.
     */
    public static void userThreadTerminated() {
        HalfBodies.end();
    }

    /**
     * Call this method at the end of the application if it completed
     * successfully, for the launcher to be aware of it.
     */
    public static void exitSuccess() {
        System.exit(0);
    }

    /**
     * Call this method at the end of the application if it did not complete
     * successfully, for the launcher to be aware of it.
     */
    public static void exitFailure() {
        System.exit(1);
    }

    public static final String PA_STARTED_PROP = "proactive.isstarted";

    public static boolean IsProActiveStarted() {
        return "true".equals(System.getProperty(PA_STARTED_PROP));
    }

    /**
     * This method should be invoked before calling any call to the ProActive Programming library.
     * It checks that ProActive is properly configured and will be able to execute successfully. If 
     * an exception is thrown, the caller should not invoke any method from the ProActive Programming
     * library since Runtime exceptions can be thrown.  
     * 
     * @throws ProActiveBadConfigurationException 
     *  If ProActive is misconfigured or if the environment does not fulfill ProActive's requirements
     */
    public static void checkConfig() throws ProActiveBadConfigurationException {
        /*
         * BE SURE TO NOT START A ProActive RUNTIME DUE TO A SIDE EFFECT
         */

        // Check an IP address is available
        try {
            ProActiveInet.getInstance().getInetAddress();
        } catch (Throwable e) {
            throw new ProActiveBadConfigurationException(
                "Unable to find a suitable IP Address. Please check your configuration", e);
        }
    }
}
