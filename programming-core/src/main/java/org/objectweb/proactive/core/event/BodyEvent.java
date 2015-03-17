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

    private static final long serialVersionUID = 61L;

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
