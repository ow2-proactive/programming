package functionalTests.component.monitoring;

import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface Service3 {
    public void foo(IntMutableWrapper i);

    public StringWrapper executeAlone();
}
