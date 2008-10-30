package functionalTests.annotations.activeobject.inputs.reject;

import java.rmi.AlreadyBoundException;

import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.annotation.activeobject.ActiveObject;
import org.objectweb.proactive.extra.annotation.migration.MigrationSignal;

@ActiveObject
public class ErrorNoMigrateTo {

	@MigrationSignal
	public void migrateTo() throws MigrationException, NodeException, AlreadyBoundException {
		System.out.println("I am defining the migration signal, but I forget to actually do the migration!");
	}
}
