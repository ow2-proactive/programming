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
package org.objectweb.proactive.core.descriptor.services;

import java.io.Serializable;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;


/**
 * A class implementing this interface represents a service to acquire JVMs
 * @author The ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public interface UniversalService extends Serializable {

    /**
     * Starts this Service
     * @return an array of ProActiveRuntime
     */
    public ProActiveRuntime[] startService() throws ProActiveException;

    /**
     * Returns the name of the service.
     * The name is static, it means that it is the same name for all instances of a
     * sefvice's class
     * @return the static name of the service
     */
    public String getServiceName();
}
