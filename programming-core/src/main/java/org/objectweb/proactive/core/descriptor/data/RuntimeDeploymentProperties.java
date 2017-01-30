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
package org.objectweb.proactive.core.descriptor.data;

import org.objectweb.proactive.core.ProActiveException;


/**
 * This class represents an array of properties that can be set at runtime when using
 * XML deployment descriptor
 * @author The ProActive Team
 * @version 1.0,  2003/04/01
 * @since   ProActive 1.0.2
 */
public class RuntimeDeploymentProperties implements java.io.Serializable {
    protected java.util.ArrayList<String> runtimeProperties;

    public RuntimeDeploymentProperties() {
        runtimeProperties = new java.util.ArrayList<String>();
    }

    protected void checkProperty(String property) throws ProActiveException {
        if (!runtimeProperties.contains(property)) {
            throw new ProActiveException("This runtime property " + property +
                                         " does not exist or has already been set!");
        }
    }
}
