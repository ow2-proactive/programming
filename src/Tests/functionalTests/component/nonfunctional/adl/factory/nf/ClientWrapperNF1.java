package functionalTests.component.nonfunctional.adl.factory.nf;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

public class ClientWrapperNF1 implements BindingController, NFService {

	final String CLIENT_ITF_1 = "ref";
	final String CLIENT_ITF_2 = "refb";
	NFService next1 = null;
	NFService next2 = null;
	final String[] itfList = new String[]{CLIENT_ITF_1, CLIENT_ITF_2};
	final String name = "[client-wrapper-nf1]";
	int turn = 0;
	
	@Override
	public StringWrapper walk() {

		StringWrapper ret;
//		if(turn == 0) {
			ret = new StringWrapper(name + next1.walk());
//		}
//		else {
//			ret = new StringWrapper(name + next2.walk());
//		}
		turn = (turn+1)%2;
		return ret;
	}

	@Override
	public void print(String msg) {
		
		if(turn == 0) {
			next1.print(msg + name);
		}
		else {
			next2.print(msg + name);
		}
		turn = (turn+1)%2;

	}

	@Override
	public void bindFc(String clientItf, Object serverItf)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if(CLIENT_ITF_1.equals(clientItf)) {
			next1 = (NFService) serverItf;
		}
		else if(CLIENT_ITF_2.equals(clientItf)) {
			next2 = (NFService) serverItf;
		}
		else throw new NoSuchInterfaceException(clientItf);

	}

	@Override
	public String[] listFc() {
		return itfList;
	}

	@Override
	public Object lookupFc(String clientItf) throws NoSuchInterfaceException {
		if(CLIENT_ITF_1.equals(clientItf)) {
			return next1;
		}
		else if(CLIENT_ITF_2.equals(clientItf)) {
			return next2;
		}
		throw new NoSuchInterfaceException(clientItf);
	}

	@Override
	public void unbindFc(String clientItf) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if(CLIENT_ITF_1.equals(clientItf)) {
			next1 = null;
		}
		else if(CLIENT_ITF_2.equals(clientItf)) {
			next2 = null;
		}
		throw new NoSuchInterfaceException(clientItf);
	}

}
