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

import org.objectweb.proactive.core.UniqueID;


/**
 * Used by the JMX Notifications
 * @author The ProActive Team
 */
public class FutureNotificationData implements Serializable {

    private static final long serialVersionUID = 62L;

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
