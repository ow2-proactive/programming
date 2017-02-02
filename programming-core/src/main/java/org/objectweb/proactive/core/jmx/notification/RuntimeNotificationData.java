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
package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.jmx.naming.FactoryName;


/**
 * Used in the JMX notifications
 * @author The ProActive Team
 */
public class RuntimeNotificationData implements Serializable {

    /** The name of the creator of the registered ProActiveRuntime */
    private String creatorID;

    /** The url of the ProActiveRuntime */
    private String runtimeUrl;

    /** The protocol used to register the registered ProActiveRuntime when created */
    private String creationProtocol;

    /** The name of the registered ProActiveRuntime */
    private String vmName;

    /**
     * Empty constructor
     */
    public RuntimeNotificationData() {
        // No args constructor
    }

    /** Creates a new RuntimeNotificationData
     * @param creatorID The name of the creator of the registered ProActiveRuntime
     * @param runtimeUrl The url of the ProActiveRuntime
     * @param creationProtocol The protocol used to register the registered ProActiveRuntime when created
     * @param vmName The name of the registered ProActiveRuntime
     */
    public RuntimeNotificationData(String creatorID, String runtimeUrl, String creationProtocol, String vmName) {
        this.creatorID = creatorID;
        this.creationProtocol = creationProtocol;
        this.vmName = vmName;

        this.runtimeUrl = FactoryName.getCompleteUrl(runtimeUrl);
    }

    /**
     * Returns The protocol used to register the registered ProActiveRuntime when created
     * @return The protocol used to register the registered ProActiveRuntime when created
     */
    public String getCreationProtocol() {
        return this.creationProtocol;
    }

    /**
     * Returns The name of the creator of the registered ProActiveRuntime
     * @return The name of the creator of the registered ProActiveRuntime
     */
    public String getCreatorID() {
        return this.creatorID;
    }

    /**
     * Returns The name of the registered ProActiveRuntime
     * @return The name of the registered ProActiveRuntime
     */
    public String getVmName() {
        return this.vmName;
    }

    /**
     * Returns The url of the ProActiveRuntime
     * @return The url of the ProActiveRuntime
     */
    public String getRuntimeUrl() {
        return this.runtimeUrl;
    }
}
