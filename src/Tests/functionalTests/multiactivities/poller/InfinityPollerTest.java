package functionalTests.multiactivities.poller;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

public class InfinityPollerTest {

	@Test(timeout=8000)
	public void noMultiActiveTest() throws ActiveObjectCreationException, NodeException, InterruptedException{
		Object[] constrPrm = { false };
		InfiniteCounter cti = PAActiveObject.newActive(InfiniteCounter.class, constrPrm);
		System.out.println("TEST: will fail to poll");
		cti.countToInfinity();
		
		for (int i=0; i<4; i++) {
			System.out.println(cti.pollValue());
			Thread.sleep(500);
		}
	}
	
	@Test(timeout=8000)
	public void multiActiveTest() throws ActiveObjectCreationException, NodeException, InterruptedException{
		Object[] constrPrm = { true };
		InfiniteCounter cti = PAActiveObject.newActive(InfiniteCounter.class, constrPrm);
		
		System.out.println("TEST: will poll values successfully");
		cti.countToInfinity();
		
		for (int i=0; i<4; i++) {
			System.out.println("I've peeked at the value and it is: "+cti.pollValue());
			Thread.sleep(500);
		}
	}
	
}
