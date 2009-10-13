package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.libvirt.LibvirtVMM;


/**
 * {@link VMMBean} implementation for Libvirt
 *
 */
public class LibvirtVMMBean implements VMMBean {
    private String uri;

    LibvirtVMMBean(String uri) {
        this.uri = uri;
    }

    /**
     * Returns {@link LibvirtVMM}
     */
    public LibvirtVMM getInstance() {
        try {
            return new LibvirtVMM(uri);
        } catch (VirtualServiceException e) {
            return null;
        }
    }

}
