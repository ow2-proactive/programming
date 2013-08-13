/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.api;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet.NotYetExposedException;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousProxy;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class provides methods to create, bind, lookup and unregister  a remote object.
 * A 'Remote Object' is a standard java object that is  remotely accessible through
 * all of the communication protocols supported by the ProActive's remote object framework.
 */
@PublicAPI
public class PARemoteObject {
    public final static Logger logger = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);

    /* Used to attribute an unique name to RO created by turnRemote */
    private final static String TURN_REMOTE_PREFIX;
    private final static String ADD_PROTOCOL_PREFIX;
    static {
        String vmName = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getName();
        TURN_REMOTE_PREFIX = "/" + vmName + "/TURN_REMOTE/";
        ADD_PROTOCOL_PREFIX = "/" + vmName + "/ADD_PROTOCOL/";
    }

    /* Used to attribute an unique name to RO created by turnRemote */
    private final static AtomicLong counter = new AtomicLong();

    public static <T> RemoteObjectExposer<T> newRemoteObject(String className, T target) {
        // The className is used for the public name of this remote object

        return new RemoteObjectExposer<T>(className, target);
    }

    public static <T> RemoteObjectExposer<T> newRemoteObject(String className, T target,
            Class<? extends Adapter<T>> targetRemoteObjectAdapter) {
        return new RemoteObjectExposer<T>(className, target, targetRemoteObjectAdapter);
    }

    public static <T> T bind(RemoteObjectExposer<T> roe, URI uri) throws ProActiveException {
        RemoteRemoteObject irro = roe.createRemoteObject(uri);
        return (T) new RemoteObjectAdapter(irro).getObjectProxy();
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
        logger.debug("Trying to lookup " + url);
        return RemoteObjectHelper.getFactoryFromURL(url).lookup(RemoteObjectHelper.expandURI(url))
                .getObjectProxy();
    }

    /**
     * Turn a POJO into a remote object.
     *
     * @param object the object to be exported as a remote object
     * @return A remote object that can be called from any JVM
     * @exception ProActiveException if the remote object cannot be created
     */
    @SuppressWarnings("unchecked")
    public static <T> T turnRemote(T object) throws ProActiveException {
        RemoteObjectExposer<T> roe = newRemoteObject(object.getClass().getName(), object);
        RemoteRemoteObject rro = roe.createRemoteObject(getUniqueName(), false);
        RemoteObjectAdapter roa = new RemoteObjectAdapter(rro);
        return (T) roa.getObjectProxy();
    }

    /**
     * Turn a POJO into a remote object.
     *
     * @param object the object to be exported as a remote object
     * @return A remote object that can be called from any JVM
     * @exception ProActiveException if the remote object cannot be created
     */
    @SuppressWarnings("unchecked")
    public static <T> T turnRemote(T object, String protocol) throws ProActiveException {
        RemoteObjectExposer<T> roe = newRemoteObject(object.getClass().getName(), object);
        RemoteRemoteObject rro = roe.createRemoteObject(getUniqueName(), false, protocol);
        RemoteObjectAdapter roa = new RemoteObjectAdapter(rro);
        return (T) roa.getObjectProxy();
    }

    /**
     * generate a unique name (that contains '/') on the current host. Should work fine even
     * with several jvm on the same host.
     * This algorithm simply uses the field 'name' from the VMinformation class and 
     * postfixes it with a counter incremented each time
     * @return a unique name (that contains '/') to serve as unique identifier
     */
    private static String getUniqueName() {
        return TURN_REMOTE_PREFIX + counter.incrementAndGet();
    }

    // -----------------------
    // = Multi-Protocol part =
    // -----------------------
    /**
     * Force usage of a specific protocol to contact an active object.
     *
     * @param obj
     *          Should be a RemoteObject
     *
     * @param protocol
     *          Can be rmi, http, pamr, rmissh, rmissl
     *
     * @throws UnknownProtocolException
     *
     * @throws NotYetExposedException
     *
     * @throws IllegalArgumentException
     *          If obj isn't an RemoteObject
     */
    public static void forceProtocol(Object obj, String protocol) throws UnknownProtocolException,
            NotYetExposedException {
        if (obj instanceof StubObject) {
            Proxy proxy = ((StubObject) obj).getProxy();
            if (proxy instanceof SynchronousProxy) {
                RemoteObject<?> ro = ((SynchronousProxy) proxy).getRemoteObject();
                // Stub side
                if (ro instanceof RemoteObjectAdapter) {
                    ((RemoteObjectAdapter) ro).forceProtocol(protocol);
                    return;
                } else {
                    throw new IllegalArgumentException(
                        "Method forceProtocol can only be called on stub object (client part of the RemoteObject)");
                }
            } else {
                throw new IllegalArgumentException("The object " + obj + " isn't a RemoteObject");
            }
        } else {
            throw new IllegalArgumentException("This method cannot be called on a POJO object");
        }
    }

    public static void forceToDefault(Object obj) throws UnknownProtocolException, NotYetExposedException {
        forceProtocol(obj, CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
    }

    public static void unforceProtocol(Object obj) throws UnknownProtocolException, NotYetExposedException {
        forceProtocol(obj, null);
    }

    public static void addProtocol(RemoteObjectExposer<?> roe, String protocol) throws ProActiveException {
        roe.createRemoteObject(ADD_PROTOCOL_PREFIX + counter.incrementAndGet(), false, protocol);
    }

}
