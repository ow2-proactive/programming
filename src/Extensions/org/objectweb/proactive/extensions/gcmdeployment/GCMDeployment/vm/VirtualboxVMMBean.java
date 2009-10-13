package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.virtualbox.VirtualboxVMM;


/**
 * {@link VMMBean} implementation for Virtualbox infrastructure.
 *
 */
public class VirtualboxVMMBean implements VMMBean {
    private String uri, user, pwd;

    public VirtualboxVMMBean(String uri, String user, String pwd) {
        this.uri = uri;
        this.user = user;
        this.pwd = pwd;
    }

    /**
     * Returns a {@link VirtualboxVMM} instance
     */
    public VirtualboxVMM getInstance() {
        try {
            return new VirtualboxVMM(uri, user, pwd);
        } catch (VirtualServiceException e) {
            return null;
        }
    }

}
