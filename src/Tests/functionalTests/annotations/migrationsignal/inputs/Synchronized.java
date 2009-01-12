package functionalTests.annotations.migrationsignal.inputs;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class Synchronized {

    @MigrationSignal
    public void doWhileOne(Node node, boolean onCondition) throws MigrationException {
        // this is sick...
        synchronized (node) {
            System.out.println("Will migrate");
            PAMobileAgent.migrateTo(node);
        }
    }

}
