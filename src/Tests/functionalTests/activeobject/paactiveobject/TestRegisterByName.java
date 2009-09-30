package functionalTests.activeobject.paactiveobject;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


public class TestRegisterByName extends FunctionalTest {

    public TestRegisterByName() {
    }

    /*
     * Check that PAActiveObject.registerByName returns the right URL
     * See PROACTIVE-741
     */
    @Test
    public void test() throws ProActiveException {
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {});

        String name = null;
        String url = null;

        name = "!";
        url = PAActiveObject.registerByName(ao, name, false);
        Assert.assertTrue(url.endsWith(name));

        name = "aaaaaa";
        url = PAActiveObject.registerByName(ao, name, false);
        Assert.assertTrue(url.endsWith(name));

        name = "zzzzzzz";
        url = PAActiveObject.registerByName(ao, name, false);
        Assert.assertTrue(url.endsWith(name));

        name = "~";
        url = PAActiveObject.registerByName(ao, name, false);
        Assert.assertTrue(url.endsWith(name));
    }

    static public class AO {
        public AO() {
        }
    }
}
