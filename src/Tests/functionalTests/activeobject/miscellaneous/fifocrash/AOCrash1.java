/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package functionalTests.activeobject.miscellaneous.fifocrash;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class AOCrash1 implements Serializable, EndActive {

    private AOCrash2 ao2;

    private StringWrapper future;

    public AOCrash1() {

    }

    public AOCrash1(AOCrash2 ao2) {
        this.ao2 = ao2;
    }

    public void foo() {
        // the foo method will trigger a receiveReply from object ao2
        future = ao2.foo2();
    }

    public boolean terminate() {
        PAActiveObject.terminateActiveObject(true);
        return true;
    }

    public void endActivity(Body body) {
        System.out.println("Expected end of activity for " + this.getClass().getName());
    }

}
