package functionalTests.annotations.callbacks.inputs;

import org.objectweb.proactive.extensions.annotation.VirtualNodeIsReadyCallback;


// correct signature "void method(String virtualNodeName)"

// error
@VirtualNodeIsReadyCallback
public class IsReady {

    // ok
    @VirtualNodeIsReadyCallback
    public void a(String p) {
    }

    // error
    @VirtualNodeIsReadyCallback
    public void a2(String p, String p2) {
    }

    // error
    @VirtualNodeIsReadyCallback
    public String a3(String p) {
        return null;
    }

    // error
    @VirtualNodeIsReadyCallback
    public void a4() {
    }
}
