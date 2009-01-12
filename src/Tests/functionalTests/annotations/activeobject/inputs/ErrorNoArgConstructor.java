package functionalTests.annotations.activeobject.inputs;

import java.io.Serializable;

import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class ErrorNoArgConstructor implements Serializable {

    public ErrorNoArgConstructor(int n) {
    }
}
