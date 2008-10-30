package functionalTests.annotations.activeobject.apt.inputs;

import java.io.Serializable;

import org.objectweb.proactive.extra.annotation.activeobject.ActiveObject;

@ActiveObject
public class ErrorSynchronizationPrimitives implements Serializable{

	// no volatile needed
	volatile boolean _synchFlag;

	// no synchronized needed
	public synchronized void checkStatus() { }

}
