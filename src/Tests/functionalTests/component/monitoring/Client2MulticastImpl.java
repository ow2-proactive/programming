package functionalTests.component.monitoring;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.ProActiveRuntimeException;


public class Client2MulticastImpl implements Runner, BindingController {
    private static final long SLEEP_TIME = 20;
    private static final String[] ITF_NAMES_FOR_EACH_METHOD = { "service2", "service2", "service2" };
    private static final String[] METHOD_NAMES = { "doAnotherThing", "getDouble", "getBoolean" };
    private static final int NB_ITERATIONS = 100;
    private Service2Multicast service2;
    private int[] nbCallsPerMethod = new int[METHOD_NAMES.length];

    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        for (int i = 0; i < METHOD_NAMES.length; i++) {
            nbCallsPerMethod[i] = 0;
        }
        for (int i = 0; i < NB_ITERATIONS; i++) {
            sleep();
            int indexMethod = i % METHOD_NAMES.length;
            nbCallsPerMethod[indexMethod]++;
            switch (indexMethod) {
                case 0:
                    service2.doAnotherThing();
                    break;
                case 1:
                    service2.getDouble();
                    break;
                case 2:
                    service2.getBoolean();
                    break;
                default:
                    break;
            }
        }
    }

    public int getTotalNbMethodCalls() {
        return NB_ITERATIONS;
    }

    public long getSleepTime() {
        return SLEEP_TIME;
    }

    public String[] getItfNamesForEachMethod() {
        return ITF_NAMES_FOR_EACH_METHOD;
    }

    public String[] getMethodNames() {
        return METHOD_NAMES;
    }

    public int[] getNbCallsPerMethod() {
        return nbCallsPerMethod;
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if ("service2".equals(clientItfName)) {
            service2 = (Service2Multicast) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] { "service2", "service3" };
    }

    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("service2".equals(clientItfName)) {
            return service2;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String arg0) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        throw new ProActiveRuntimeException("not implemented!");
    }
}
