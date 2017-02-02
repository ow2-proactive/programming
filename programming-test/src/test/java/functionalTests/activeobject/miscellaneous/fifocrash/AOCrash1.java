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
package functionalTests.activeobject.miscellaneous.fifocrash;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.api.PAActiveObject;


public class AOCrash1 implements Serializable, EndActive {

    private AOCrash2 ao2;

    public AOCrash1() {

    }

    public AOCrash1(AOCrash2 ao2) {
        this.ao2 = ao2;
    }

    public void foo() {
        // the foo method will trigger a receiveReply from object ao2
        ao2.foo2();
    }

    public boolean terminate() {
        PAActiveObject.terminateActiveObject(true);
        return true;
    }

    public void endActivity(Body body) {
        System.out.println("Expected end of activity for " + this.getClass().getName());
    }

}
