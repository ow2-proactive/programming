package functionalTests.annotations.migrationsignal.inputs;

import static org.objectweb.proactive.api.PAMobileAgent.migrateTo;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class AcceptBlock {

    @MigrationSignal
    public void freakyShiet(Node node) throws MigrationException {
        {
            {
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                PAMobileAgent.migrateTo(node);
            }
        }
        {
            {
                {
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    migrateTo(node);
                }
            }
            {
                System.out.println();
                System.out.println();
                org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
            }
        }
    }

}
