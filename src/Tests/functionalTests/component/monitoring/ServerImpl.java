package functionalTests.component.monitoring;

import java.util.List;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class ServerImpl implements Service1, Service2, Service3Gathercast {
    public static final long EXECUTION_TIME = 50;

    private void sleep() {
        try {
            Thread.sleep(EXECUTION_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doSomething() {
        sleep();
    }

    public IntWrapper getInt() {
        sleep();
        return null;
    }

    public StringWrapper hello() {
        sleep();
        return null;
    }

    public void doAnotherThing() {
        sleep();
    }

    public DoubleWrapper getDouble() {
        sleep();
        return null;
    }

    public BooleanWrapper getBoolean() {
        sleep();
        return null;
    }

    public List<StringWrapper> executeAlone() {
        sleep();
        return null;
    }

    public void foo(List<IntMutableWrapper> i) {
        sleep();
    }

}
