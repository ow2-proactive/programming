package functionalTests.annotations.remoteobject.inputs;

import org.objectweb.proactive.extensions.annotation.RemoteObject;


// 1 warning - constructor is not empty
@RemoteObject
public class ErrorNonEmptyConstructor {

    public ErrorNonEmptyConstructor() {
        String tata = "mama";
        tata.substring(3);
    }

    public ErrorNonEmptyConstructor(String z) {
    }
}
