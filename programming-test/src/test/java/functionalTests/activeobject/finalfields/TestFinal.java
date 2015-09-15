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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.activeobject.finalfields;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.GCMFunctionalTest;


/**
 * This test check that final fields are correctly handled by ProActive
 */
public class TestFinal extends GCMFunctionalTest {
    Node node;
    Receiver receiver;

    public TestFinal() throws ProActiveException {
        super(1, 1);
        super.startDeployment();
    }

    @Before
    public void createReceiver() throws ActiveObjectCreationException, NodeException {
        Node node = super.getANode();
        receiver = PAActiveObject.newActive(Receiver.class, new Object[] {}, node);
    }

    public void test(Class<? extends Data> cl) throws InstantiationException, IllegalAccessException {
        Data data = cl.newInstance();
        UniqueID localUniqID = data.get();

        receiver.setData(data);
        UniqueID remoteUniqID = receiver.getData().get();

        Assert.assertEquals(localUniqID, remoteUniqID);
    }

    @Test
    public void testData1() throws ActiveObjectCreationException, NodeException, InstantiationException,
            IllegalAccessException {
        test(Data1.class);
    }

    @Test
    public void testData2() throws ActiveObjectCreationException, NodeException, InstantiationException,
            IllegalAccessException {
        test(Data2.class);
    }

    @Test
    public void testTurnActiveData1() throws ActiveObjectCreationException, NodeException,
            InterruptedException {
        Data data = new Data1();
        Data activeData = (Data) PAActiveObject.turnActive(data, node);

        Assert.assertEquals(data.get(), activeData.get());
        Assert.assertEquals(activeData.get(), data.get());

    }

    @Test
    public void testTurnActiveData2() throws ActiveObjectCreationException, NodeException,
            InterruptedException {
        Data data = new Data2();
        Data activeData = (Data) PAActiveObject.turnActive(data, node);

        Assert.assertEquals(data.get(), activeData.get());
    }

    public static class Receiver implements Serializable {
        Data data;

        public Receiver() {
        }

        public void setData(Data data) {
            this.data = data;
        }

        public Data getData() {
            return data;
        }

    }

    public interface Data {
        public UniqueID get();
    }

    public static class Data1 implements Data, Serializable {
        final UniqueID uniqueID = new UniqueID();

        public UniqueID get() {
            return uniqueID;
        }
    }

    public static class Data2 implements Data, Serializable {
        final UniqueID uniqueID;

        public Data2() {
            this.uniqueID = new UniqueID();
        }

        public UniqueID get() {
            return uniqueID;
        }
    }
}
