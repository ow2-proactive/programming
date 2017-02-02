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
package functionalTests.multiprotocol;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * AOMultiProtocolSwitch
 *
 * @author The ProActive Team
 */
public class AOMultiProtocolSwitch {

    public AOMultiProtocolSwitch() {

    }

    public BooleanWrapper foo() {
        return new BooleanWrapper(true);
    }

    public BooleanWrapper foo2() {
        longWait();
        return new BooleanWrapper(true);
    }

    public BooleanWrapper autocont() {
        return ((AOMultiProtocolSwitch) PAActiveObject.getStubOnThis()).foo();
    }

    public BooleanWrapper autocont2() {
        longWait();
        return ((AOMultiProtocolSwitch) PAActiveObject.getStubOnThis()).foo2();
    }

    public boolean bar() {
        return true;
    }

    public boolean waitPlease() {
        longWait();
        return true;
    }

    public BooleanWrapper waitPlease2() {
        longWait();
        return new BooleanWrapper(true);
    }

    public boolean throwException() {
        throw new RuntimeException("Expected Exception");
    }

    private void longWait() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean disableProtocol(String protocol) {
        AbstractBody myBody = (AbstractBody) PAActiveObject.getBodyOnThis();

        RemoteObjectExposer<UniversalBody> roe = ((AbstractBody) myBody).getRemoteObjectExposer();

        System.out.println("trying to disable " + protocol);

        try {
            roe.disableProtocol(protocol);
        } catch (Exception e) {

        }
        return true;
    }

}
