package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.vmwarevix.VMwareVMM;
import org.ow2.proactive.virtualizing.vmwarevix.VMwareVMM.Service;


/**
 * {@link VMMBean} implementation for VMwareVix library
 *
 */
public class VMwareVixVMMBean implements VMMBean {
    private String uri, user, pwd;
    private int port = 904;
    private Service service = Service.vmwareDefault;

    public VMwareVixVMMBean(String uri, String user, String pwd, int port, Service service) {
        this.uri = uri;
        this.user = user;
        this.pwd = pwd;
        this.port = port;
        this.service = service;
    }

    /**
     * returns {@link VMwareVMM} instance
     */
    public VMwareVMM getInstance() {
        try {
            return new VMwareVMM(uri, user, pwd, port, service);
        } catch (VirtualServiceException e) {
            return null;
        }
    }

}
