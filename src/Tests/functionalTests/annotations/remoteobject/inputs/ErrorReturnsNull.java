package functionalTests.annotations.remoteobject.inputs;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.annotation.RemoteObject;


@RemoteObject
public class ErrorReturnsNull {

    // error - active object method returning null
    public String testing() {
        return null;
    }

    private String testing2() {
        return null;
    }

    // should report error only once!
    public ErrorReturnsNull getNodes() {
        Object[] params = new Object[0];
        ErrorReturnsNull lookup_active = null;
        try {
            lookup_active = (ErrorReturnsNull) PAActiveObject.newActive(ErrorReturnsNull.class.getName(),
                    params);
        } catch (ActiveObjectCreationException e) {
            //logger.fatal("Couldn't create an active lookup", e);
            return null;
        } catch (NodeException e) {
            //logger.fatal("Couldn't connect node to creat", e);
            return null;
        }
        return lookup_active;
    }
}
