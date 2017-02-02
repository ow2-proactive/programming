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
package functionalTests.activeobject.miscellaneous.deadlocks.usecase1;

import java.io.Serializable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;


public class AODeadlock1 implements Serializable, InitActive, RunActive {

    private AODeadlock1 stub;

    private AODeadlock2 ao2;

    public AODeadlock1() {

    }

    public AODeadlock1(AODeadlock2 ao2) {
        this.ao2 = ao2;
    }

    public void callback() {
        System.out.println("Callback");
    }

    public int foo() {
        return ao2.answer().getIntValue();
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        try {
            while (true) {

                service.waitForRequest();

                service.serveAll("foo");
                service.serveAll("callback");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void initActivity(Body body) {
        stub = (AODeadlock1) PAActiveObject.getStubOnThis();

    }

    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        AODeadlock2 ao2 = PAActiveObject.newActive(AODeadlock2.class, new Object[0]);
        AODeadlock1 ao1 = PAActiveObject.newActive(AODeadlock1.class, new Object[] { ao2 });
        ao2.setAODeadlock1(ao1);

        int iw = ao1.foo();
        System.out.println(iw);

    }
}
