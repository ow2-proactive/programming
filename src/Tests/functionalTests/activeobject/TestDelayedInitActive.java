package functionalTests.activeobject;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.Sleeper;

import functionalTests.FunctionalTest;


/**
 * Test case for PROACTIVE-652
 */
public class TestDelayedInitActive extends FunctionalTest {
    static final long SLEEP = 200;
    static final long EPSYLON = (long) (SLEEP / 10);

    @Test
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {});
        long before = System.currentTimeMillis();
        /* Race condition: PAActiveObject.setIS() has not yet been called when ao.is() is served
         * The request is put into the request queue instead of being served as an IS -> asynchronous call 
         */
        ao.is();
        long after = System.currentTimeMillis();
        Assert.assertTrue("Method call seems to be async but should be sync (immediate service)", after -
            before >= SLEEP);
    }

    static public class AO implements Serializable, InitActive {
        public void initActivity(Body body) {
            // Enlarge the race condition window
            raceConditionHelper();

            PAActiveObject.setImmediateService("is");
        }

        private void raceConditionHelper() {
            new Sleeper(100).sleep();
        }

        public void is() {
            new Sleeper(SLEEP).sleep();
        }
    }
}
