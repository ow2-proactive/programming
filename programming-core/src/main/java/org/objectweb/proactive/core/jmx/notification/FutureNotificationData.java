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

import org.objectweb.proactive.core.UniqueID;


/**
 * Used by the JMX Notifications
 * @author The ProActive Team
 */
public class FutureNotificationData implements Serializable {

    /**
     * UniqueID of the body which wait this future
     */
    private UniqueID bodyID;

    /**
     * UniqueID of the body which create this future
     */
    private UniqueID creatorID;

    public FutureNotificationData() {
        // No args constructor
    }

    /**
     * Creates an new FuturNotificationData, used by JMX Notification
     * @param bodyID UniqueID of the body which wait this future
     * @param creatorID UniqueID of the body which create this future
     */
    public FutureNotificationData(UniqueID bodyID, UniqueID creatorID) {
        this.bodyID = bodyID;
        this.creatorID = creatorID;
    }

    /**
     * Returns the UniqueID of the body which wait this future
     * @return The UniqueID of the body which wait this future
     */
    public UniqueID getBodyID() {
        return bodyID;
    }

    /**
     * Returns the UniqueID of the body which create this future
     * @return The UniqueID of the body which create this future
     */
    public UniqueID getCreatorID() {
        return creatorID;
    }
}
