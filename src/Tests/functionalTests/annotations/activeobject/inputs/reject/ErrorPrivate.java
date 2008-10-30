package functionalTests.annotations.activeobject.inputs.reject;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.extra.annotation.activeobject.ActiveObject;
import org.objectweb.proactive.extra.annotation.migration.MigrationSignal;

@ActiveObject
public class ErrorPrivate {
	// error - private method
	@MigrationSignal
	private void migrateTo1() throws MigrationException {
		PAMobileAgent.migrateTo(new Object());
	}

	@MigrationSignal
	protected void migrateTo2() throws MigrationException {
		PAMobileAgent.migrateTo(new Object());
	}

	@MigrationSignal
	void migrateTo3() throws MigrationException {
		PAMobileAgent.migrateTo(new Object());
	}

}
