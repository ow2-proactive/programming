package functionalTests.component.monitoring;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface Service1 {
    public IntWrapper getInt();

    public void doSomething();

    public StringWrapper hello();
}
