package functionalTests.annotations.activeobject.apt.inputs;

import java.io.Serializable;
import java.util.List;

@org.objectweb.proactive.extra.annotation.activeobject.ActiveObject
public class Reject{
	public Reject(int n) {}
	private volatile List<Object> _someLocks;
			private synchronized void doNothingSynchronized() {}
	private final int dontOverrideMe() { return 0; }

	public int _counter;

	//public int getCounter() { return _counter; }

	public void setCounter(int counter) { _counter = counter; }
}