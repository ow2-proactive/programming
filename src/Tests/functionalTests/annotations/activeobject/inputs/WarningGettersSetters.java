package functionalTests.annotations.activeobject.inputs;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class WarningGettersSetters implements Serializable {

    // WARNING (no accessors)
    public int error;

    // WARNING (bad accessors name)
    public int _counter;

    public void setCounter(IntWrapper counter) {
        _counter = counter.intValue();
    }

    public IntWrapper getCounter() {
        return new IntWrapper(_counter);
    }

    // WARNING (bad accessors name)
    public String name;

    public StringWrapper getname() {
        return new StringWrapper(name);
    }

    public void setName(String name) {
    }

    // OK
    String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    // OK
    String _test;

    public String get_test() {
        return _test;
    }

    public void set_test(String _test) {
        this._test = _test;
    }

    // WARNING package access
    int test2;

    // OK (non public)
    private int test3;

    // WARNING (only getter)
    public int test4;

    public int getTest4() {
        return test4;
    }

    // WARNING (only setter)
    public int test5;

    public void setTest5(int test5) {
        this.test5 = test5;
    }

    // OK (final field)
    final Object o = new Object();

    // OK - P2PAcquaintanceManager inspired
    private int NOA;

    public int getMaxNOA() {
        return NOA;
    }

    public void setMaxNOA(int noa) {
        NOA = noa;
    }

    public int getNOAMax() {
        return NOA;
    }

    public void setNOAMax(int noa) {
        NOA = noa;
    }

    public int getMaxNOAMinPayne() {
        return NOA;
    }

    public void setMaxPayneNOAMin(int noa) {
        NOA = noa;
    }

}
