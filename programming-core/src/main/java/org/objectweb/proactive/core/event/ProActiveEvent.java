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

/**
 * <p>
 * Base class of all events occuring in ProActive. <code>ProActiveEvent</code>
 * provides a event type and a timestamp.
 * </p><p>
 * Should be subclassed to create more specific events.
 * </p>
 *
 * @see java.util.EventObject
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class ProActiveEvent extends java.util.EventObject implements java.io.Serializable {
    public static final int GENERIC_TYPE = -1;

    /** type of the message */
    protected int type;

    /** The timestamp */
    protected long timeStamp;

    /**
     * Creates a new <code>ProActiveEvent</code> based on the given object and type
     * @param obj the object originating of the event
     * @param type the type of the event
     */
    public ProActiveEvent(Object obj, int type) {
        super(obj);
        this.timeStamp = System.currentTimeMillis();
        this.type = type;
    }

    /**
     * Returns the time this event was created
     * @return the time this event was created
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns the type of this event
     * @return the type of this event
     */
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ProActiveEvent@" + timeStamp + " type=" + type;
    }
}
