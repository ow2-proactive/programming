package functionalTests.annotations.migrationstrategy.inputs;

import org.objectweb.proactive.extensions.annotation.OnArrival;
import org.objectweb.proactive.extensions.annotation.OnDeparture;


public class ErrorParameters {

    // ERR - has parameters
    @OnDeparture
    public void leavingBad(double ou, Class where) {
    }

    // ERR - has parameters
    @OnArrival
    public void arrivingBad(int id, String where) {
    }

    @OnDeparture
    public void leavingOk() {
    }

    @OnArrival
    public void arrivingOk() {
    }

}
