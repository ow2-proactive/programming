package functionalTests.component.nonfunctional.adl.factory.nf;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

public class ServerWrapperNF2 implements BindingController, NFService {

	final String CLIENT_ITF = "ref";
	final String CLIENTEND_ITF = "refend";
	NFService next = null;
	NFServiceEnd end = null;
	final String[] itfList = new String[]{CLIENT_ITF, CLIENTEND_ITF};
	final String name = "[server-wrapper-nf2]";
	
	@Override
	public StringWrapper walk() {

		StringWrapper ret;
		ret = new StringWrapper(name + end.walkEnd());
		return ret;
	}

	@Override
	public void print(String msg) {
		
		end.printEnd(msg + name);

	}

	@Override
	public void bindFc(String clientItf, Object serverItf)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if(CLIENT_ITF.equals(clientItf)) {
			next = (NFService) serverItf;
		}
		else if(CLIENTEND_ITF.equals(clientItf)) {
			end = (NFServiceEnd) serverItf;
		}
		else throw new NoSuchInterfaceException(clientItf);

	}

	@Override
	public String[] listFc() {
		return itfList;
	}

	@Override
	public Object lookupFc(String clientItf) throws NoSuchInterfaceException {
		if(CLIENT_ITF.equals(clientItf)) {
			return next;
		}
		else if(CLIENTEND_ITF.equals(clientItf)) {
			return end;
		}
		throw new NoSuchInterfaceException(clientItf);
	}

	@Override
	public void unbindFc(String clientItf) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if(CLIENT_ITF.equals(clientItf)) {
			next = null;
		}
		else if(CLIENTEND_ITF.equals(clientItf)) {
			end = null;
		}
		throw new NoSuchInterfaceException(clientItf);
	}

}
