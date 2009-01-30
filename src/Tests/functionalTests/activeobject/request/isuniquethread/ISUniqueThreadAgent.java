package functionalTests.activeobject.request.isuniquethread;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

public class ISUniqueThreadAgent implements InitActive{


	private Thread isCallingThread1;
	private Thread isCallingThread2;
	private Thread isCallingThread3;
	private boolean call3Failed;

	public ISUniqueThreadAgent(){}


	public BooleanWrapper isCall1(){
		if (this.isCallingThread1==null){
			this.isCallingThread1 = Thread.currentThread();
		}
		return new BooleanWrapper(this.isCallingThread1 == Thread.currentThread() && this.isCallingThread2 != Thread.currentThread());
	}

	public BooleanWrapper isCall2(){
		if (this.isCallingThread2==null){
			this.isCallingThread2 = Thread.currentThread();
		}
		return new BooleanWrapper(this.isCallingThread2 == Thread.currentThread() && this.isCallingThread1 != Thread.currentThread());
	}

	public void isCall3(){
		if (this.isCallingThread3==null){
			this.isCallingThread3 = Thread.currentThread();
		}
		if (this.isCallingThread3 != Thread.currentThread()) {
			this.call3Failed = true;
		}
	}


	public BooleanWrapper getTestResultForCall3() {
		return new BooleanWrapper(this.call3Failed);
	}


	public void initActivity(Body body) {
		PAActiveObject.setImmediateService("isCall1");
		PAActiveObject.setImmediateService("isCall2");
		PAActiveObject.setImmediateService("isCall3");
	}


	public void stdCall(){
		if (this.isCallingThread1 != Thread.currentThread()) {
			System.out.println(" STD CALL : not the same thread");
		}
	}

}
