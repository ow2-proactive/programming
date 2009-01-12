package functionalTests.annotations.migrationsignal.inputs;

import static org.objectweb.proactive.api.PAMobileAgent.migrateTo;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class TryCatchFinally {

    @MigrationSignal
    public void migrateTryCatch(Node node, String nr) throws MigrationException {
        try {
            Integer.parseInt(nr);
            Object o = nr;
            Double d = (Double) o;
            PAMobileAgent.migrateTo(node);
        } catch (NumberFormatException e) {
            migrateTo(node);
        } catch (ClassCastException e) {
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        }
    }

    @MigrationSignal
    public void migrateTryCatchFinallyGood(Node node, String nr) throws MigrationException {
        try {
            Integer.parseInt(nr);
            Object o = nr;
            Double d = (Double) o;
        } catch (NumberFormatException e) {
            System.out.println("I want number not " + nr);
        } catch (ClassCastException e) {
            System.out.println("WTF?!");
        } finally {
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        }

    }

    @MigrationSignal
    public void migrateTryCatchFinallyNo(Node node, String nr) throws MigrationException {
        try {
            Integer.parseInt(nr);
            Object o = nr;
            Double d = (Double) o;
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        } catch (NumberFormatException e) {
            System.out.println("I want number not " + nr);
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        } catch (ClassCastException e) {
            System.out.println("WTF?!");
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        } finally {
            System.out.println("Here should be migrateTo");
        }

    }

}
