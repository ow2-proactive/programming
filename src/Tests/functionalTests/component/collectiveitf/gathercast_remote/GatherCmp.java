package functionalTests.component.collectiveitf.gathercast_remote;

import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class GatherCmp implements GatherItf, Runnable, BindingController {
    ClientItf clientItf;

    public void receiveData(List<Object> args) {
        System.out.println("GatherCmp.receiveData()" + args.get(0));
    }

    public void run() {
        System.out.println("GatherCmp.run()");
        clientItf.receiveData("arg");
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if ("sender".equals(clientItfName)) {
            clientItf = (ClientItf) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] { "sender" };
    }

    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("sender".equals(clientItfName)) {
            return clientItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if ("sender".equals(clientItfName)) {
            clientItf = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
