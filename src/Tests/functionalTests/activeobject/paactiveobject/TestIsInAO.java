package functionalTests.activeobject.paactiveobject;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


public class TestIsInAO extends FunctionalTest {

    @Test
    public void test1() {
        Assert.assertFalse(PAActiveObject.isInActiveObject());
    }

    @Test
    public void test2() throws ActiveObjectCreationException, NodeException {
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {});
        boolean resp = ao.isInAO();
        System.out.println(resp);
        Assert.assertTrue(resp);
    }

    @Test
    public void test3() throws ActiveObjectCreationException, NodeException {
        AO noao = new AO();
        boolean resp = noao.isInAO();
        System.out.println(resp);
        Assert.assertFalse(resp);
    }

    static public class AO {
        public AO() {
        }

        public boolean isInAO() {
            return PAActiveObject.isInActiveObject();
        }
    }
}
