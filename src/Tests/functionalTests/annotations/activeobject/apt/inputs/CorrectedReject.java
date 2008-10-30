package functionalTests.annotations.activeobject.apt.inputs;

import java.io.Serializable;
import java.util.List;

public class CorrectedReject implements Serializable {

	public CorrectedReject() {}

	public CorrectedReject(int n) {}

	private List<Object> _someLocks;

	private void doNothingSynchronized() {}

	private int dontOverrideMe() { return 0; }

	public int _counter;

	public int getCounter() { return _counter; }

	public void setCounter(int counter) { _counter = counter; }
}
