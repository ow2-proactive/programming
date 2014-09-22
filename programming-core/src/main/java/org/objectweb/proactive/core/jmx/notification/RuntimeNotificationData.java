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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.jmx.naming.FactoryName;


/**
 * Used in the JMX notifications
 * @author The ProActive Team
 */
public class RuntimeNotificationData implements Serializable {

    private static final long serialVersionUID = 60L;

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
