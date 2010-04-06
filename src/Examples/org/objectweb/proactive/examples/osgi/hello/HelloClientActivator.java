package org.objectweb.proactive.examples.osgi.hello;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


//@snippet-start HelloClientActivator_1
public class HelloClientActivator implements BundleActivator {

    public void start(BundleContext arg0) throws Exception {

        String filter = "(out=system)";
        ServiceReference[] sr = arg0.getServiceReferences(HelloService.class.getName(), filter);

        if (sr == null) {
            System.out.println("Couldn't start this bundle: The service '" + HelloService.class.getName() +
                "' [filter = " + filter + "]is not started");
            return;
        }

        HelloService h = (HelloService) arg0.getService(sr[0]);

        h.sayHello();
        h.saySomething("Hello ProActive Team!");

        filter = "(out=logger)";
        sr = arg0.getServiceReferences(HelloService.class.getName(), filter);

        if (sr == null) {
            System.out.println("Couldn't start this bundle: The service '" + HelloService.class.getName() +
                "' [filter = " + filter + "]is not started");
            return;
        }

        h = (HelloService) arg0.getService(sr[0]);

        h.sayHello();
        h.saySomething("Hello ProActive Team!");

    }

    public void stop(BundleContext arg0) throws Exception {
        // TODO Auto-generated method stub
    }

}
//@snippet-end HelloClientActivator_1