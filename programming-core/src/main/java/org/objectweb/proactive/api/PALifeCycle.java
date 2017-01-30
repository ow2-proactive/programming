/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.api;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.exceptions.ProActiveBadConfigurationException;
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
            throw new ProActiveBadConfigurationException("Unable to find a suitable IP Address. Please check your configuration",
                                                         e);
        }
    }
}
