package functionalTests.annotations.activeobject.inputs.reject;

import java.rmi.AlreadyBoundException;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.annotation.activeobject.ActiveObject;
import org.objectweb.proactive.extra.annotation.migration.MigrationSignal;

@ActiveObject
public class ErrorNotLastBlock {

	// error -not last statement
	@MigrationSignal
	public String migrateTo() {
		try {
			org.objectweb.proactive.api.PAMobileAgent.migrateTo("");
			System.out.println("I am such an awful ProActive migration user!");
			return ""; // the sweet C-style hakz

		} catch (MigrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

}
