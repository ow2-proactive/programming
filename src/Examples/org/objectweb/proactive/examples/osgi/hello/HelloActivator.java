package org.objectweb.proactive.examples.osgi.hello;

import java.util.Properties;

import org.objectweb.proactive.examples.hello.Hello;
import org.objectweb.proactive.extensions.osgi.ProActiveService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class HelloActivator implements BundleActivator {

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
        this.proActiveService = (ProActiveService) this.context.getService(sr);
        Hello h = (Hello) this.proActiveService.newActive(
                "org.objectweb.proactive.examples.hello.Hello",
                new Object [] {});


        /* Register the service */
        Properties props = new Properties();
        props.put("name", "helloWorld");

        this.context.registerService(
                "org.objectweb.proactive.osgi.ProActiveService",
                h, props);
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    }

}
