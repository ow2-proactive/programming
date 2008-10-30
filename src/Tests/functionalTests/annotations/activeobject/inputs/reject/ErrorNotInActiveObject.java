package functionalTests.annotations.activeobject.inputs.reject;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.extra.annotation.migration.MigrationSignal;

public class ErrorNotInActiveObject {
	// the containig class is not annotated using ActiveObject
	@MigrationSignal
	public void migrateTo() throws MigrationException {
		PAMobileAgent.migrateTo(new Object());
	}
}