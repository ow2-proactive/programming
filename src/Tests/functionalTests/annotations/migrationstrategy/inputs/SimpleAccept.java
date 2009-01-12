package functionalTests.annotations.migrationstrategy.inputs;

import org.objectweb.proactive.extensions.annotation.OnArrival;
import org.objectweb.proactive.extensions.annotation.OnDeparture;


public class SimpleAccept {

    @OnDeparture
    public void leaving() {
        System.out.println("Leaving Node...");
    }

    @OnArrival
    public void arriving() {
        System.out.println("Finally home...");
    }
}
