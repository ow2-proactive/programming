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
package org.objectweb.proactive.extensions.pamr;

import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Before ProActive 5.0.0 PAMR loggers were exported into a wrong namespace 
 * {@link Loggers#CORE}.forwarding. ProActive 5.0.0 fixed this issue and moved all 
 * the PAMR loggers into the {@link PAMRConfig.Loggers#PAMR} namespaces. 
 * 
 * This class ensure backward compatibility. If user configured the logger in the old
 * namespace then their configuration is automatically applied to the new namespace.
 * 
 * @author ProActive team
 * @since  ProActive 5.0.0
 */
public class PAMRLog4jCompat {

    public void ensureCompat() {
        loadLogger(PAMRConfig.Loggers.PAMR);
        loadLogger(PAMRConfig.Loggers.PAMR_CLASSLOADING);
        loadLogger(PAMRConfig.Loggers.PAMR_CLIENT);
        loadLogger(PAMRConfig.Loggers.PAMR_CLIENT_TUNNEL);
        loadLogger(PAMRConfig.Loggers.PAMR_MESSAGE);
        loadLogger(PAMRConfig.Loggers.PAMR_REMOTE_OBJECT);
        loadLogger(PAMRConfig.Loggers.PAMR_ROUTER);
        loadLogger(PAMRConfig.Loggers.PAMR_ROUTER_ADMIN);
    }

    private void loadLogger(String newLoggerName) {
        String oldName = newLoggerName.replaceAll(Loggers.CORE + ".pamr", Loggers.CORE + ".forwarding");
        ProActiveLogger.getLogger(newLoggerName, oldName);
    }
}
