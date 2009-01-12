package functionalTests.annotations.remoteobject.inputs;

import org.objectweb.proactive.extensions.annotation.RemoteObject;


@RemoteObject
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
