package functionalTests.activeobject.request.isuniquethread;

import java.util.Vector;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import functionalTests.GCMFunctionalTestDefaultNodes;

public class TestISUniqueThread extends GCMFunctionalTestDefaultNodes {


	public TestISUniqueThread(int hostCapacity, int vmCapacity) {
		super(hostCapacity, vmCapacity);
	}

	public TestISUniqueThread () {
		super(2,1);
	}

	@Test
	public void test() throws ActiveObjectCreationException, NodeException {

		Node n1 = super.getANode();
		Node n2 = super.getANode();


		ISUniqueThreadAgent a1 = (ISUniqueThreadAgent)PAActiveObject.newActive(ISUniqueThreadAgent.class.getName(), null, n1);
		ISUniqueThreadAgent a2 = (ISUniqueThreadAgent)PAActiveObject.newActive(ISUniqueThreadAgent.class.getName(), null, n2);

		Vector<BooleanWrapper> a1res1 = new Vector<BooleanWrapper>();
		Vector<BooleanWrapper> a2res1 = new Vector<BooleanWrapper>();
		for (int i=0;i<10;i++){
			a1res1.add(a1.isCall1());
			a2res1.add(a2.isCall1());
		}

		Vector<BooleanWrapper> a1res2 = new Vector<BooleanWrapper>();
		Vector<BooleanWrapper> a2res2 = new Vector<BooleanWrapper>();
		for (int i=0;i<10;i++){
			a1res2.add(a1.isCall2());
			a2res2.add(a2.isCall2());
		}

		for (int i=0;i<10;i++){
			a1.isCall3();
			a2.isCall3();
		}
		BooleanWrapper a1res3 = a1.getTestResultForCall3();
		BooleanWrapper a2res3 = a2.getTestResultForCall3();

		// assert call 1 and 2
		for (int i=0;i<10;i++) {
			assert (a1res1.get(i).booleanValue() && a2res1.get(i).booleanValue() && a1res2.get(i).booleanValue() && a2res2.get(i).booleanValue());
		}

		// assert call 3
		assert (a1res3.booleanValue() && a2res3.booleanValue());

	}


}
