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
package functionalTests.activeobject.request;

import java.io.Serializable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


/**
 * @author The ProActive Team
 */
public class A implements Serializable, RunActive {

    /**
     *
     */
    int counter = 0;

    // for ACs after termination test
    private A delegate;

    public A() {
    }

    public void initDeleguate() {
        try {
            this.delegate = PAActiveObject.newActive(A.class, new Object[0]);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    public void runActivity(Body body) {
        body.blockCommunication();
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        body.acceptCommunication();
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        service.fifoServing();
    }

    public void method1() {
        counter++;
    }

    public A method2() {
        counter++;
        return new A();
    }

    public int method3() {
        counter++;
        return counter;
    }

    public StringWrapper getValue() {
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new StringWrapper("Returned value");
    }

    public StringWrapper getDelegateValue() {
        return this.delegate.getValue();
    }

    public void exit() throws Exception {
        PAActiveObject.terminateActiveObject(true);
    }
}
