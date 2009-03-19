package org.objectweb.proactive.core.util.log.remote;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;


/**
 * Deploys a {@link ProActiveLogCollector} on the local runtime
 * 
 * It is a simple wrapper to easily get the URL at which the collector is bound.
 */
final public class ProActiveLogCollectorDeployer {
    /** URL of the remote object */
    final private String url;

    /** The collector */
    final private ProActiveLogCollector collector;

    final RemoteObjectExposer<ProActiveLogCollector> roe;

    public ProActiveLogCollectorDeployer(String name) {
        this.collector = new ProActiveLogCollector();
        this.roe = PARemoteObject.newRemoteObject(ProActiveLogCollector.class.getName(), this.collector);
        this.roe.createRemoteObject(name);
        this.url = roe.getURL();
    }

    /** Get the local log collector*/
    public ProActiveLogCollector getCollector() {
        return this.collector;
    }

    /** Get the log collector as a remote object */
    public ProActiveLogCollector getRemoteObject() throws ProActiveException {
        return (ProActiveLogCollector) RemoteObjectHelper.generatedObjectStub(this.roe.getRemoteObject());
    }

    /** Get the URL of the local log collector */
    public String getCollectorURL() {
        return this.url;
    }

    /** Unexport the remote object
     * @throws ProActiveException  
     */
    public void terminate() throws ProActiveException {
        roe.unexportAll();
    }
}
