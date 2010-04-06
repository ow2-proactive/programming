package org.objectweb.proactive.examples.osgi.hello;

import java.util.Properties;

import org.objectweb.proactive.extensions.osgi.ProActiveService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


//@snippet-start HelloActivator_1
//@snippet-start HelloActivator_2
public class HelloActivator implements BundleActivator {
    //@snippet-break HelloActivator_1

    private BundleContext context;
    private ProActiveService proActiveService;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        this.context = context;

        /* gets the ProActive Service */
        ServiceReference sr = this.context.getServiceReference(ProActiveService.class.getName());

        if (sr == null) {
            System.out.println("Couldn't start this bundle: The service '" +
                ProActiveService.class.getName() + "' is not started");
            return;
        }

        this.proActiveService = (ProActiveService) this.context.getService(sr);
        HelloService h = (HelloService) this.proActiveService.newActive(HelloSystemOut.class.getName(),
                new Object[] {});

        /* Register the service */
        Properties props = new Properties();
        props.put("name", "HelloSystemOut");
        props.put("out", "system");

        this.context.registerService(HelloService.class.getName(), h, props);

        h = (HelloService) this.proActiveService.newActive(HelloLogInfo.class.getName(), new Object[] {});

        /* Register the service */
        props = new Properties();
        props.put("name", "HelloLogInfo");
        props.put("out", "logger");

        this.context.registerService(HelloService.class.getName(), h, props);
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    }

    //@snippet-resume HelloActivator_1
}
//@snippet-end HelloActivator_1
//@snippet-end HelloActivator_2