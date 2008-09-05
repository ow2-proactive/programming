package functionalTests.component.monitoring;

import java.util.List;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface Service1Multicast {
    public List<IntWrapper> getInt();

    public void doSomething();

    public List<StringWrapper> hello();
}