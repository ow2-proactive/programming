package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.vmwarevi.VMwareVMM;


/**
 * {@link VMMBean} implementation for VMwareVI
 *
 */
public class VMwareVIVMMBean implements VMMBean {
    private String uri, user, pwd;

    public VMwareVIVMMBean(String uri, String user, String pwd) {
        this.uri = uri;
        this.user = user;
        this.pwd = pwd;
    }

    /**
     * returns a {@link VMwareVMM} instance
     */
    public VMwareVMM getInstance() {
        try {
            return new VMwareVMM(uri, user, pwd);
        } catch (VirtualServiceException e) {
            return null;
        }
    }

}
