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

import org.objectweb.proactive.core.UniqueID;


/**
 * <p>
 * A <code>FutureEvent</code> occurs when a <code>FuturProxy</code>
 * blocks the executing Thread because the result is not yet available.
 * </p>
 *
 * @see org.objectweb.proactive.core.body.future.FutureProxy
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class FutureEvent extends ProActiveEvent implements java.io.Serializable {

    private static final long serialVersionUID = 60L;

    /** Created when a Thread is blocked. */
    public static final int WAIT_BY_NECESSITY = 10;

    /** Created when a Thread continues */
    public static final int RECEIVED_FUTURE_RESULT = 20;
    private UniqueID creatorID;

    /**
     * Creates a new <code>FutureEvent</code> based on the given FutureProxy
     * @param bodyID the <code>UniqueID</code> of the body that is waiting for the future result
     * @param creatorID the <code>UniqueID</code> of the body that created
     * the corresponding <code>Future</code>
     * @param type the type of the event that occured
     */
    public FutureEvent(UniqueID bodyID, UniqueID creatorID, int type) {
        super(bodyID, type);
        this.creatorID = creatorID;
    }

    /**
     * Returns the <code>UniqueID</code> of the body that created the corresponding <code>Future</code>
     * @return the <code>UniqueID</code> of the body that created the corresponding <code>Future</code>
     */
    public UniqueID getCreatorID() {
        return creatorID;
    }

    /**
     * Returns the <code>UniqueID</code> of the body that is waiting
     * @return the <code>UniqueID</code> of the body that is waiting
     */
    public UniqueID getBodyID() {
        return (UniqueID) getSource();
    }

    @Override
    public String toString() {
        return "FutureEvent bodyID=" + getBodyID() + " creatorID=" + getCreatorID();
    }
}
