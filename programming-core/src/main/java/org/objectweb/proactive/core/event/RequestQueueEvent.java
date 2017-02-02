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

import org.objectweb.proactive.core.UniqueID;


/**
 * <p>
 * A <code>RequestQueueEvent</code> occurs when a <code>RequestQueue</code> get modified.
 * </p>
 *
 * @see org.objectweb.proactive.core.body.request.RequestQueue
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class RequestQueueEvent extends ProActiveEvent implements java.io.Serializable {
    public static final int ADD_REQUEST = 10;

    public static final int REMOVE_REQUEST = 40;

    public static final int WAIT_FOR_REQUEST = 60;

    /** id of the object owner of the Queue */
    protected UniqueID ownerID;

    /**
     * Creates a new <code>RequestQueueEvent</code> based on the given owner id and type
     * @param ownerID the id of the owner of the <code>RequestQueue</code> in which the event occured
     * @param type the type of the event that occured
     */
    public RequestQueueEvent(UniqueID ownerID, int type) {
        super(ownerID, type);
        this.ownerID = ownerID;
    }

    /**
     * Returns the id of the owner of the <code>RequestQueue</code> in which the event occured
     * @return the id of the owner of the <code>RequestQueue</code> in which the event occured
     */
    public UniqueID getOwnerID() {
        return ownerID;
    }
}
