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

import org.objectweb.proactive.core.body.UniversalBody;


/**
 * <p>
 * Event sent when a body get created, destroyed or changed.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class BodyEvent extends ProActiveEvent implements java.io.Serializable {

    /** constant for the creation of a body */
    public static final int BODY_CREATED = 10;

    /** constant for the deletion of a body */
    public static final int BODY_DESTROYED = 20;

    /** constant for the changed of a body */
    public static final int BODY_CHANGED = 30;

    /**
     * Creates a new <code>BodyEvent</code>
     * @param body the body created or deleted
     * @param messageType the type of the event either BODY_CREATED or BODY_DESTROYED
     */
    public BodyEvent(UniversalBody body, int messageType) {
        super(body, messageType);
    }

    /**
     * Returns the body associated to this event
     * @return the body associated to this event
     */
    public UniversalBody getBody() {
        return (UniversalBody) getSource();
    }
}
