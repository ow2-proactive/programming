package functionalTests.annotations.activeobject.inputs;

import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class ErrorFinalMethods {

    // ERROR
    public final void doSomething() {
    }

    // ERROR
    final void doSomething2() {
    }

    // OK
    private final void doSomething3() {
    }
}
