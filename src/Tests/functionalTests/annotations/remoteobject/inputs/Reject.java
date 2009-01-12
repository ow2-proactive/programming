package functionalTests.annotations.remoteobject.inputs;

import java.util.List;

import org.objectweb.proactive.extensions.annotation.RemoteObject;


@RemoteObject
public class Reject {
    public Reject(int n) {
    } // E no-arg constructor

    private volatile List<Object> _someLocks;

    private synchronized void doNothingSynchronized() {
    }

    private final int dontOverrideMe() {
        return 0;
    } //E final method

    public int _counter; // W no getters/setters

    //public int getCounter() { return _counter; }

    public void setCounter(int counter) {
        _counter = counter;
    }
}