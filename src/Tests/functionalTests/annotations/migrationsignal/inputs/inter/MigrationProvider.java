package functionalTests.annotations.migrationsignal.inputs.inter;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


public class MigrationProvider {
    @MigrationSignal
    public void migrateTo() throws MigrationException {
        PAMobileAgent.migrateTo(new Object());
    }
}