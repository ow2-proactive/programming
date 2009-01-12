package functionalTests.annotations.migrationsignal.inputs;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class Loopz {

    @MigrationSignal
    public void doWhileOne(Node node, boolean onCondition) throws MigrationException {
        do {
            System.out.println("Will migrate");
            doWhileTwo(node);
        } while (onCondition);
    }

    @MigrationSignal
    public void doWhileTwo(Node node) throws MigrationException {
        do
            PAMobileAgent.migrateTo(node);
        while (true);
    }

    @MigrationSignal
    public void WhileOne(Node node, boolean onCondition) throws MigrationException {
        while (onCondition) {
            System.out.println("Will migrate");
            doWhileOne(node, onCondition);
            System.out.println("not!");
        }
    }

    @MigrationSignal
    public void WhileTwo(Node node) throws MigrationException {
        while (true)
            PAMobileAgent.migrateTo(node);
    }

    @MigrationSignal
    public void ForOne(Node node, boolean onCondition) throws MigrationException {
        for (;;) {
            System.out.println("Will migrate");
            doWhileOne(node, onCondition);
            return; // this will actually cause an error!!! :)
        }
    }

    @MigrationSignal
    public void ForTwo(Node node) throws MigrationException {
        for (;;)
            PAMobileAgent.migrateTo(node);
    }

    @MigrationSignal
    public void EnhancedForOne(Node node, boolean onCondition) throws MigrationException {
        String[] stuffs = new String[] { "one", "two", "three" };
        for (String stuff : stuffs) {
            System.out.println("Will migrate");
            doWhileOne(node, onCondition);
        }

    }

    @MigrationSignal
    public void EnhancedForTwo(Node node) throws MigrationException {
        String[] stuffs = new String[] { "one", "two", "three" };
        for (String stuff : stuffs)
            PAMobileAgent.migrateTo(node);
    }

}
