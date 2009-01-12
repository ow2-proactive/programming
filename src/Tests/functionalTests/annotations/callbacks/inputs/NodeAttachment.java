package functionalTests.annotations.callbacks.inputs;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.NodeAttachmentCallback;


// correct signature "void method(Node, String virtualNodeName)"

// error
@NodeAttachmentCallback
public class NodeAttachment {

    // ok
    @NodeAttachmentCallback
    public void a(Node node, String p) {
    }

    // error
    @NodeAttachmentCallback
    public void a2(String p, String p2) {
    }

    // error
    @NodeAttachmentCallback
    public void a3(Node p) {
    }

    // error
    @NodeAttachmentCallback
    public void a4() {
    }
}
