package functionalTests.component.monitoring;

import java.util.List;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;


public interface Service2Multicast {
    public void doAnotherThing();

    public List<DoubleWrapper> getDouble();

    public List<BooleanWrapper> getBoolean();
}