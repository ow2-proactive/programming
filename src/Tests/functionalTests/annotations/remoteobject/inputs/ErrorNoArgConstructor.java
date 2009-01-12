package functionalTests.annotations.remoteobject.inputs;

import java.io.Serializable;

import org.objectweb.proactive.extensions.annotation.RemoteObject;


@RemoteObject
public class ErrorNoArgConstructor implements Serializable {

    public ErrorNoArgConstructor(int n) {
    }
}
