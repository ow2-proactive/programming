package functionalTests.annotations.migrationsignal.inputs;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


public class ErrorNotInActiveObject {
    // the containig class is not annotated using ActiveObject
    @MigrationSignal
    public void migrateTo() throws MigrationException {
        PAMobileAgent.migrateTo(new Object());
    }
}