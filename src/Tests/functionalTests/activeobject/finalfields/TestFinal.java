package functionalTests.activeobject.finalfields;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.GCMFunctionalTestDefaultNodes;


/**
 * This test check that final fields are correctly handled by ProActive
 */
public class TestFinal extends GCMFunctionalTestDefaultNodes {
    Node node;
    Receiver receiver;

    public TestFinal() {
        super(1, 1);
    }

    @Before
    public void createReceiver() throws ActiveObjectCreationException, NodeException {
        Node node = super.getANode();
        receiver = (Receiver) PAActiveObject.newActive(Receiver.class.getName(), new Object[] {}, node);
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
