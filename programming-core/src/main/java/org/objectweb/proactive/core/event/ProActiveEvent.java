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

    private static final long serialVersionUID = 62L;
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
