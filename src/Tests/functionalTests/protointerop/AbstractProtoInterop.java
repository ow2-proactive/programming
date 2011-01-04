/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionalTests.protointerop;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet.NotYetExposedException;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.annotation.ActiveObject;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class AbstractProtoInterop extends GCMFunctionalTestDefaultNodes {

    public AbstractProtoInterop(String protocol) {
        super(1, 1);
        super.vContract.setVariableFromProgram("jvmargDefinedByTest", "-Dproactive.communication.protocol=" +
            protocol, VariableContractType.DescriptorDefaultVariable);
    }

    @Test(timeout = 10000)
    public void test() throws ActiveObjectCreationException, NodeException, UnknownProtocolException,
            NotYetExposedException {
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
