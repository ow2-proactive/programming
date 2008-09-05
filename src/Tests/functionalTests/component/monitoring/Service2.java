package functionalTests.component.monitoring;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;


public interface Service2 {
    public void doAnotherThing();

    public DoubleWrapper getDouble();

    public BooleanWrapper getBoolean();
}
