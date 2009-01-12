package functionalTests.annotations.migrationsignal.inputs;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;

import functionalTests.annotations.migrationsignal.inputs.inter.MigrationProvider;


@ActiveObject
public class AcceptInterClassCall {

    private AnotherMigrateTo ao = new AnotherMigrateTo();
    private MigrationProvider mig = new MigrationProvider();

    // call against a local variable
    @MigrationSignal
    public void migrateTo1() throws MigrationException {
        AnotherMigrateTo amt = new AnotherMigrateTo();
        amt.migrateTo();
    }

    //    @MigrationSignal
    //    public void migrateTo2() throws MigrationException {
    //        // a more sophisticated form of call
    //        new AnotherMigrateTo().migrateTo();
    //    }

    // local variable; type fully named
    @MigrationSignal
    public void migrateTo2() throws ProActiveException {
        functionalTests.annotations.migrationsignal.inputs.inter.MigrationProvider mp = new MigrationProvider();
        mp.migrateTo();
    }

    // local variable; type imported
    @MigrationSignal
    public void migrateTo3() throws ProActiveException {
        MigrationProvider mp = new MigrationProvider();
        mp.migrateTo();
    }

    // call against a field member, class in the same compilation unit
    @MigrationSignal
    public void migrateTo4() throws MigrationException {
        ao.migrateTo();
    }

    // call against a field member, class in different package
    @MigrationSignal
    public void migrateTo5() throws MigrationException {
        mig.migrateTo();
    }

}

class AnotherMigrateTo {

    @MigrationSignal
    public void migrateTo() throws MigrationException {
        PAMobileAgent.migrateTo(new Object());
    }
}
