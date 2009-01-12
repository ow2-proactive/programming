package functionalTests.annotations.migrationsignal.inputs;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class Switch {

    // 1 error - because of the case 2 block, the while instruction
    @MigrationSignal
    public void switchme(Node node, int flag, boolean onCond, String nr) throws MigrationException {

        switch (flag) {
            case 0:
                System.out.println("MIgrating");
                PAMobileAgent.migrateTo(node);
                break;
            case 1:
                try {
                    Integer.parseInt(nr);
                    Object o = nr;
                    Double d = (Double) o;
                    PAMobileAgent.migrateTo(node);
                } catch (NumberFormatException e) {
                    PAMobileAgent.migrateTo(node);
                } catch (ClassCastException e) {
                    org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
                }
                break;
            case 2:
                while (onCond) {
                    System.out.println("Will migrate");
                    org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
                }
                break;
            default:
                if (onCond) {
                    System.out.println("will migrate");
                    PAMobileAgent.migrateTo(node);
                } else
                    PAMobileAgent.migrateTo(node);
                break;
        }

    }
}
