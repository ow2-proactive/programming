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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;


/**
 * <p>
 * Event sent when a proActiveRuntime is registered in a Hashtable.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/08/06
 * @since   ProActive 0.9.4
 *
 */
public class RuntimeRegistrationEvent extends ProActiveEvent {

    /** constant for the registration */
    public static final int RUNTIME_REGISTERED = 10;

    public static final int RUNTIME_ACQUIRED = 20;

    public static final int RUNTIME_UNREGISTERED = 30;

    protected String creatorID;

    protected ProActiveRuntime registeredRuntimeName;

    protected String protocol;

    protected String vmName;

    /**
     * Creates a new <code>RuntimeRegistrationEvent</code>
     * @param proActiveRuntime the local runtime on which the registration occurs
     * @param messageType the type of the event RUNTIME_REGISTERED
     * @param registeredRuntimeName the name of the registered ProActiveRuntime
     * @param creatorID The name of the creator of the registered ProActiveRuntime
     * @param protocol The protocol used to register the registered ProActiveRuntime when created
     */
    public RuntimeRegistrationEvent(ProActiveRuntime proActiveRuntime, int messageType,
            ProActiveRuntime registeredRuntimeName, String creatorID, String protocol, String vmName) {
        super(proActiveRuntime, messageType);
        this.creatorID = creatorID;
        this.registeredRuntimeName = registeredRuntimeName;
        this.protocol = protocol;
        this.vmName = vmName;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public ProActiveRuntime getRegisteredRuntime() {
        return registeredRuntimeName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getVmName() {
        return vmName;
    }
}
