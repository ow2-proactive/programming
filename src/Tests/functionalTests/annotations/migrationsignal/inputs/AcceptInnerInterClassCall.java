package functionalTests.annotations.migrationsignal.inputs;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


// this is inspired from src/Examples/org/objectweb/proactive/examples/chat/ChatGUI.java
public class AcceptInnerInterClassCall {

    private Migration ao = new Migration();

    class InnerMigration {
        @MigrationSignal
        public void actionPerformed(boolean cond) {
            if (cond) {
                ao.migrateTo();
            } else {
                System.out.println("Hold your ground!");
            }
        }
    }

}

class Migration {

    @MigrationSignal
    public void migrateTo() {
        try {
            PAMobileAgent.migrateTo(new Object());
        } catch (MigrationException e) {
            System.out.println("Tasty exception!");
        }
    }
}