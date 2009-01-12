package functionalTests.annotations.migrationstrategy.inputs;

import org.objectweb.proactive.extensions.annotation.OnArrival;
import org.objectweb.proactive.extensions.annotation.OnDeparture;


@OnDeparture
class MisplacedOnDeparture {
}

@OnArrival
class MisplacedOnArrival {

}

public class MisplacedAnnotation {

}
