/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.annotations.activeobject.inputs;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class ErrorReturnsNull {

    // error - active object method returning null
    public String testing() {
        return null;
    }

    // ok -private
    private String testing2() {
        return null;
    }

    // should report error only once!
    public ErrorReturnsNull getNodes() {
        Object[] params = new Object[0];
        ErrorReturnsNull lookup_active = null;
        try {
            lookup_active = (ErrorReturnsNull) PAActiveObject.newActive(ErrorReturnsNull.class.getName(),
                    params);
        } catch (ActiveObjectCreationException e) {
            //logger.fatal("Couldn't create an active lookup", e);
            return null;
        } catch (NodeException e) {
            //logger.fatal("Couldn't connect node to creat", e);
            return null;
        }
        return lookup_active;
    }
}
