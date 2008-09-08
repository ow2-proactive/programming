package functionalTests.component.monitoring;

import java.util.List;

import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface Service3Gathercast {
    public void foo(List<IntMutableWrapper> i);

    public List<StringWrapper> executeAlone();
}
