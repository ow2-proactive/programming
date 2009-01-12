package functionalTests.annotations.migrationstrategy.inputs;

import org.objectweb.proactive.extensions.annotation.OnArrival;
import org.objectweb.proactive.extensions.annotation.OnDeparture;


public class ErrorReturnType {

    // ERR - return type must be void
    @OnDeparture
    public String leaving() {
        return "";
    }

    @OnArrival
    public int arriving() {
        return 42;
    }
}
