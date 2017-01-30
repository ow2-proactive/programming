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
package functionalTests.protointerop;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet.NotYetExposedException;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;

import functionalTests.GCMFunctionalTest;


public class AbstractProtoInterop extends GCMFunctionalTest {

    public AbstractProtoInterop(String protocol) throws ProActiveException {
        super(1, 1);
        super.setOptionalJvmParamters("-Dproactive.communication.protocol=" + protocol);
        super.startDeployment();
    }

    public void test()
            throws ActiveObjectCreationException, NodeException, UnknownProtocolException, NotYetExposedException {
        Node node = super.getANode();

        DummyAO local = (DummyAO) PAActiveObject.newActive(DummyAO.class, new Object[] {});
        DummyAO remote = (DummyAO) PAActiveObject.newActive(DummyAO.class, new Object[] { local }, node);
        remote.check();
        remote.checkReverse();
    }

    @ActiveObject
    public static class DummyAO {
        DummyAO remote;

        public DummyAO() {
            // Do nothing
        }

        public DummyAO(DummyAO remote) {
            this.remote = remote;
        }

        public BooleanWrapper check() {
            return new BooleanWrapper(true);
        }

        public BooleanWrapper checkReverse() {
            return new BooleanWrapper(this.remote.check().getBooleanValue()); // Disable AC
        }
    }
}
