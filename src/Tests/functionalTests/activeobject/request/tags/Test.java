package functionalTests.activeobject.request.tags;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;

import functionalTests.FunctionalTest;


/**
 * Tests for Message Tagging on the Request
 * 
 * 1/ Check the propagation of a Tag and its data
 * 2/ Check the non propagation of a Tag
 * 3/ Check the local Memory of a Tag
 *
 */
public class Test extends FunctionalTest {

    private A activeA;
    private int propagationResult;
    private int localmemoryValue1, localmemoryValue2;
    private boolean stopPropagationResult, noMemoryOnB;
    private boolean leaseExceededCleaningDone;
    private boolean leaseRenew;

    @Before
    public void action() throws Exception {
        PAProperties.PA_MEMORY_TAG_LEASE_PERIOD.setValue(5);
        PAProperties.PA_MAX_MEMORY_TAG_LEASE.setValue(10);
        activeA = (A) PAActiveObject.newActive(A.class.getName(), new Object[0]);
        activeA.initialize();
    }

    @org.junit.Test
    public void propagation() {
        propagationResult = activeA.propagateTag();
        stopPropagationResult = activeA.stopPropagateTag();

        assertTrue(propagationResult == 42);
        assertTrue(stopPropagationResult);

    }

    @org.junit.Test
    public void localMemory() {
        localmemoryValue1 = activeA.localMemory1();
        localmemoryValue2 = activeA.localMemory2();
        noMemoryOnB = activeA.checkNoLocalMemoryOnB();

        assertTrue(localmemoryValue1 == 0);
        assertTrue(localmemoryValue2 == 1);
        assertTrue(noMemoryOnB);
    }

    @org.junit.Test
    public void leaseCheck() {
        assertTrue(activeA.localMemoryLeaseExceeded());
        assertTrue(activeA.localMemoryLeaseClean2());
    }

}
