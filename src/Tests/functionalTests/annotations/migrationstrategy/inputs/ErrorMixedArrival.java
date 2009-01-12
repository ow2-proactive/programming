package functionalTests.annotations.migrationstrategy.inputs;

import org.objectweb.proactive.extensions.annotation.OnArrival;


public class ErrorMixedArrival {

    // 2xERR :
    // 1) has parameters
    // 2) returns something
    @OnArrival
    String leaving(String where) {
        return where + " for Christmas";
    }

}
