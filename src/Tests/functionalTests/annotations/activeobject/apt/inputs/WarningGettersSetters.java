package functionalTests.annotations.activeobject.apt.inputs;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.annotation.activeobject.ActiveObject;

@ActiveObject
public class WarningGettersSetters implements Serializable{

	public int error;

	// OK, should recognize coding conventions
	public int _counter;
	public void setCounter(IntWrapper counter) { _counter = counter.intValue(); }
	public IntWrapper getCounter() { return new IntWrapper(_counter); }

	// OK, should be case-sensitive
	public String name;
	public StringWrapper getname() {return new StringWrapper(name);}
	public void setName(String name) { }
}
