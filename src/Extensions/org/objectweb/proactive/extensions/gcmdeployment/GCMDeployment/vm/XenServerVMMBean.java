package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.xenserver.XenServerVMM;


/**
 * {@link VMMBean} implementation for XenServer platform
 */
public class XenServerVMMBean implements VMMBean {
    private String uri, user, pwd;

    XenServerVMMBean(String uri, String user, String pwd) {
        this.uri = uri;
        this.user = user;
        this.pwd = pwd;
    }

    /**
     * returns {@link XenServerVMM}
     */
    public XenServerVMM getInstance() {
        try {
            return new XenServerVMM(uri, user, pwd);
        } catch (VirtualServiceException e) {
            return null;
        }
    }
}
