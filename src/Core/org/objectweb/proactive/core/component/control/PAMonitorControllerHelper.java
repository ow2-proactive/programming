/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.control;

import org.etsi.uri.gcm.api.control.MonitorController;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Useful methods for the monitor controller.
 *
 * @author The ProActive Team
 * @see MonitorController
 */
@PublicAPI
public class PAMonitorControllerHelper {
    private static final String KEY_INFO_SEPARATOR = "-";

    /**
     * Generate an unique key according to the name of the server interface, the name of the method
     * and the class names of the parameters of the method.
     *
     * @param itfName Name of the server interface where the method is exposed.
     * @param methodName Name of the method.
     * @param parametersTypes Types of the parameters of the method.
     * @return Key built like this itfName-MethodName-ClassNameParam1-ClassNameParam2-...
     */
    public static String generateKey(String itfName, String methodName, Class<?>[] parametersTypes) {
        String key = itfName + KEY_INFO_SEPARATOR + methodName;

        if (parametersTypes != null) {
            for (int i = 0; i < parametersTypes.length; i++) {
                key += KEY_INFO_SEPARATOR + parametersTypes[i].getName();
            }
        }

        return key;
    }
}
