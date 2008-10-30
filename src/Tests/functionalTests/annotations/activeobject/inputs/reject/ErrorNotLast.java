package functionalTests.annotations.activeobject.inputs.reject;

import java.rmi.AlreadyBoundException;
import java.util.Random;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.annotation.activeobject.ActiveObject;
import org.objectweb.proactive.extra.annotation.migration.MigrationSignal;

@ActiveObject
public class ErrorNotLast {

	// error - not last statement
	@MigrationSignal
	public void migrateTo1() throws MigrationException, NodeException, AlreadyBoundException {
		int i=0;
		PAMobileAgent.migrateTo(NodeFactory.createNode(""));
		i++; // muhahaw
	}

	@MigrationSignal
	public void migrateTo2() throws MigrationException, NodeException, AlreadyBoundException {
		int i=0;
		PAMobileAgent.migrateTo(NodeFactory.createNode(""));
		i++; // muhahaw
		System.out.println("Migration done!");
	}

	// a more subtle error - the return statement actually contains
	// another method call - the call to the Integer constructor
	@MigrationSignal
	public Integer migrateTo3() throws MigrationException, NodeException, AlreadyBoundException {
		int i=0;
		org.objectweb.proactive.api.PAMobileAgent.migrateTo(NodeFactory.createNode(""));
		return new Integer(i);
	}

	// error - another method call after migrateTo call
	@MigrationSignal
	public int migrateTo4() throws MigrationException, NodeException, AlreadyBoundException {
		Random r = new Random();
		org.objectweb.proactive.api.PAMobileAgent.migrateTo(NodeFactory.createNode(""));
		return r.nextInt();
	}

}
