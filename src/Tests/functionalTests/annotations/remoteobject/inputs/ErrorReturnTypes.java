package functionalTests.annotations.remoteobject.inputs;

import java.io.Serializable;

import org.objectweb.proactive.extensions.annotation.RemoteObject;


@RemoteObject
public class ErrorReturnTypes implements Serializable {

    public int _counter;

    public void setCounter(int counter) {
        _counter = counter;
    }

    // error - int is not reifiable
    public int getCounter() {
        return _counter;
    }

    // error - String is not reifiable; use StringWrapper
    public String getMyName() {
        return "BIG_DADDY";
    }

    // error - array not reifiable
    public Object[] whatYouKnow() {
        return new Object[] { /*nothing*/};
    }

    enum Truth {
        TRUE, FALSE, MAYBE
    };

    // error - enums not reifiable
    public Truth politics() {
        return Truth.MAYBE;
    }

    // OK, this object is reifiable
    public ErrorReturnTypes getInstance() {
        return this;
    }
}
