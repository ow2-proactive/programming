package functionalTests.multiactivities.pingpong;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.multiactivity.MultiActiveService;

public class PingPongTest {
	
	/**
	 * This one will timeout, becaue the reentrant code is not working in standard PA
	 * @throws ActiveObjectCreationException
	 * @throws NodeException
	 */
	@Test(timeout=8000)
	public void testNoMultiActive() throws ActiveObjectCreationException, NodeException{
		Object[] constrPrm = { false };
		Pinger a = PAActiveObject.newActive(Pinger.class, constrPrm);
		Pinger b = PAActiveObject.newActive(Pinger.class, constrPrm);
		a.setOther(b);
		b.setOther(a);
		
		System.out.println();
		System.out.println("Test: should see one ping");
		System.out.println(a.startWithPing());
	}
	
	/**
	 * This will work fine, because we have multi-activity :D
	 * @throws ActiveObjectCreationException
	 * @throws NodeException
	 */
	@Test
	public void testMultiActive() throws ActiveObjectCreationException, NodeException{
		Object[] constrPrm = { true };
		Pinger a = PAActiveObject.newActive(Pinger.class, constrPrm);
		Pinger b = PAActiveObject.newActive(Pinger.class, constrPrm);
		a.setOther(b);
		b.setOther(a);
		
		System.out.println();
		System.out.println("Test: should see ping-pongs");
		System.out.println(a.startWithPing());
	}
	
	/**
	 * This one will also freeze up because the startWithPong() method is not annotated to run with someone else...
	 * @throws ActiveObjectCreationException
	 * @throws NodeException
	 */
	@Test(timeout=8000)
	public void testMultiActiveBadAnnotation() throws ActiveObjectCreationException, NodeException{
		Object[] constrPrm = { true };
		Pinger a = PAActiveObject.newActive(Pinger.class, constrPrm);
		Pinger b = PAActiveObject.newActive(Pinger.class, constrPrm);
		a.setOther(b);
		b.setOther(a);
		
		System.out.println();
		System.out.println("Test: should see one pong");
		System.out.println(a.startWithPong());
	}
	
}
