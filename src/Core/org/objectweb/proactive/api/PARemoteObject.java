package org.objectweb.proactive.api;

import java.net.URI;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;


/**
 * This class provides methods to create, bind, lookup and unregister  a remote object.
 * A 'Remote Object' is a standard java object that is  remotely accessible through
 * all of the communication protocols supported by the ProActive's remote object framework.
 */
@PublicAPI
public class PARemoteObject {

    public static <T> RemoteObjectExposer<T> newRemoteObject(String className, T target) {
        return new RemoteObjectExposer<T>(className, target);
    }

    public static <T> RemoteObjectExposer<T> newRemoteObject(String className, T target,
            Class<? extends Adapter<T>> targetRemoteObjectAdapter) {
        return new RemoteObjectExposer<T>(className, target, targetRemoteObjectAdapter);
    }

    public static <T> T activateProtocol(RemoteObjectExposer<T> roe, URI uri) throws UnknownProtocolException {
        InternalRemoteRemoteObject irro = roe.activateProtocol(uri);
        try {
            return (T) new RemoteObjectAdapter(irro).getObjectProxy();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * List all the remote objects contained within a registry
     * @param url url of the registry
     * @return return the list of objects (not only remote objects) registered in the registry identified by the param url
     * @throws ProActiveException
     */
    public static URI[] list(URI url) throws ProActiveException {
        return RemoteObjectHelper.getFactoryFromURL(url).list(RemoteObjectHelper.expandURI(url));
    }

    /**
     * unregister the object located at the endpoint identified by the url
     * @param url
     * @throws ProActiveException
     */
    public static void unregister(URI url) throws ProActiveException {
        RemoteObjectHelper.getFactoryFromURL(url).unregister(RemoteObjectHelper.expandURI(url));
    }

    /**
     * perform a lookup on the url in order to retrieve a stub on the object exposed through
     * a remote object
     * @param url the url to lookup
     * @return a stub on the object exposed by the remote object
     * @throws ProActiveException
     */
    public static Object lookup(URI url) throws ProActiveException {
        return RemoteObjectHelper.getFactoryFromURL(url).lookup(RemoteObjectHelper.expandURI(url))
                .getObjectProxy();
    }

}
