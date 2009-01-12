package functionalTests.annotations.remoteobject.inputs;

import org.objectweb.proactive.extensions.annotation.RemoteObject;


@RemoteObject
public class PrivateEmptyConstructor {
    private PrivateEmptyConstructor() {
    }

    public PrivateEmptyConstructor(String s) {
    }
}
