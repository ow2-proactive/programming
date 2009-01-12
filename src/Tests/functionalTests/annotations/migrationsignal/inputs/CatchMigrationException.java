package functionalTests.annotations.migrationsignal.inputs;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class CatchMigrationException {

    // OK simple case
    @MigrationSignal
    public void migrateTo(Node where) {
        try {
            PAMobileAgent.migrateTo(where);
        } catch (MigrationException migExcp) {
            System.err.println("Reporting the error");
            migExcp.printStackTrace();
        }
    }

    // OK superclass
    @MigrationSignal
    public void migrateTo2(Node where) {
        try {
            PAMobileAgent.migrateTo(where);
        } catch (Exception migExcp) {
            System.err.println("Reporting the error");
            migExcp.printStackTrace();
        }
    }

    // OK superclass
    @MigrationSignal
    public void migrateTo3(Node where) {
        try {
            PAMobileAgent.migrateTo(where);
        } catch (ProActiveException migExcp) {
            System.err.println("Reporting the error");
            migExcp.printStackTrace();
        }
    }

    @MigrationSignal
    public void migrateTryCatch(Node node, String nr) {
        try {
            Integer.parseInt(nr);
            Object o = nr;
            Double d = (Double) o;
            PAMobileAgent.migrateTo(node);
        } catch (NumberFormatException e) {
            migrateTo(node);
        } catch (ClassCastException e) {
            try {
                org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
            } catch (MigrationException migExcp) {
                // freaky shiet
            }
        } catch (org.objectweb.proactive.core.body.migration.MigrationException migExcp) {
            System.err.println("Reporting the error");
            migExcp.printStackTrace();
        }
    }
}
