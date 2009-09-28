/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.core.naming;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;


/**
 * Deploys {@link NamingService} instance on the local runtime.
 */
public class NamingServiceDeployer {

    private static final String NAMING_SERVICE_DEFAULT_NAME = "defaultNamingService";

    /** URL of the remote object */
    final private String url;

    final private NamingService namingService;

    RemoteObjectExposer<NamingService> roe;

    /**
     * Deploys locally a NamingService instance as a RemoteObject with default name.
     */
    public NamingServiceDeployer() throws ProActiveException {
        this(NAMING_SERVICE_DEFAULT_NAME);
    }

    /**
     * Deploys locally a NamingService instance as a RemoteObject with specified name.
     *
     * @param name
     *            of deployed RemoteObject
     */
    public NamingServiceDeployer(String name) throws ProActiveException {
        namingService = new NamingService();

        roe = PARemoteObject.newRemoteObject(NamingService.class.getName(), this.namingService);
        roe.createRemoteObject(name, false);
        url = roe.getURL();
    }

    public NamingService getLocalNamingService() {
        return this.namingService;
    }

    public NamingService getRemoteNamingService() throws ProActiveException {
        return (NamingService) RemoteObjectHelper.generatedObjectStub(this.roe.getRemoteObject());
    }

    public String getNamingServiceURL() {
        return this.url;
    }

    /**
     * Unexport the remote object.
     *
     * @throws ProActiveException
     */
    public void terminate() throws ProActiveException {
        if (roe != null) {
            roe.unexportAll();
            roe = null;
        }
    }
}
