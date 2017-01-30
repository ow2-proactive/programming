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
package org.objectweb.proactive.core.jmx;

import java.util.HashMap;


/**
 *  Constants for ProActive JMX ServerConnector
 * @author The ProActive Team
 *
 */
public class ProActiveJMXConstants {
    public static final String PROTOCOL = "proactive";

    public static final String SERVER_REGISTERED_NAME = "PAJMXServer";

    public static final HashMap<String, String> PROACTIVE_JMX_ENV = new HashMap<String, String>();

    static {
        PROACTIVE_JMX_ENV.put("jmx.remote.protocol.provider.pkgs", "org.objectweb.proactive.core.jmx.provider");
    }
}
