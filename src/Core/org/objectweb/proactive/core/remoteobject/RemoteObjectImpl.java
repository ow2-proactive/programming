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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.security.PublicKey;


/**
 *         Implementation of a remote object.
 *
 *
 */

public class RemoteObjectImpl<T> implements RemoteObject<T>, Serializable {

    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);
    protected Object target;
    protected String className;
    protected String proxyClassName;
    protected Class<Adapter<T>> adapterClass;
    protected ProActiveSecurityManager psm;
    protected RemoteObjectExposer<T> roe;
    protected String name;

    public RemoteObjectImpl(String name, String className, T target) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        this(name, className, target, null);
    }

    public RemoteObjectImpl(String name, String className, T target, Class<Adapter<T>> adapter)
            throws IllegalArgumentException, SecurityException, InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        this(name, className, target, adapter, null);
    }

    public RemoteObjectImpl(String name, String className, T target, Class<Adapter<T>> adapter,
            ProActiveSecurityManager psm) throws IllegalArgumentException, SecurityException,
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        this.target = target;
        this.name = name;
        this.className = className;
        this.proxyClassName = SynchronousProxy.class.getName();
        //        this.adapter =  adapter.getConstructor(Object.class).newInstance(target);
        this.adapterClass = adapter;
        this.psm = psm;
    }

    public Reply receiveMessage(Request message) throws RenegotiateSessionException, ProActiveException {
        try {
            if (message.isCiphered() && (this.psm != null)) {
                message.decrypt(this.psm);
            }
            Object o;

            if (message instanceof RemoteObjectRequest) {
                o = ((RemoteObjectRequest) message).execute(this);
            } else {
                o = message.getMethodCall().execute(this.target);
            }

            return new SynchronousReplyImpl(new MethodCallResult(o, null));
        } catch (MethodCallExecutionFailedException e) {
            //            e.printStackTrace();
            throw new ProActiveException(e);
        } catch (InvocationTargetException e) {
            return new SynchronousReplyImpl(new MethodCallResult(null, e.getCause()));
        }
    }

    // implements SecurityEntity ----------------------------------------------
    public TypedCertificate getCertificate() throws SecurityNotAvailableException, IOException {
        if (this.psm != null) {
            return this.psm.getCertificate();
        }
        throw new SecurityNotAvailableException();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getEntities()
     */
    public Entities getEntities() throws SecurityNotAvailableException, IOException {
        if (this.psm != null) {
            return this.psm.getEntities();
        }
        throw new SecurityNotAvailableException();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getPolicy(org.objectweb.proactive.core.security.securityentity.Entities, org.objectweb.proactive.core.security.securityentity.Entities)
     */
    public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException {
        if (this.psm == null) {
            throw new SecurityNotAvailableException();
        }
        return this.psm.getPolicy(local, distant);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getPublicKey()
     */
    public PublicKey getPublicKey() throws SecurityNotAvailableException, IOException {
        if (this.psm != null) {
            return this.psm.getPublicKey();
        }
        throw new SecurityNotAvailableException();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#publicKeyExchange(long, byte[])
     */
    public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException, IOException {
        if (this.psm != null) {
            return this.psm.publicKeyExchange(sessionID, signature);
        }
        throw new SecurityNotAvailableException();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
            RenegotiateSessionException, IOException {
        if (this.psm != null) {
            return this.psm.randomValue(sessionID, clientRandomValue);
        }
        throw new SecurityNotAvailableException();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
            byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
            throws SecurityNotAvailableException, RenegotiateSessionException, IOException {
        if (this.psm != null) {
            return this.psm.secretKeyExchange(sessionID, encodedAESKey, encodedIVParameters,
                    encodedClientMacKey, encodedLockData, parametersSignature);
        }
        throw new SecurityNotAvailableException();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#startNewSession(long, org.objectweb.proactive.core.security.SecurityContext, org.objectweb.proactive.core.security.TypedCertificate)
     */
    public long startNewSession(long distantSessionID, SecurityContext policy,
            TypedCertificate distantCertificate) throws SecurityNotAvailableException, SessionException {
        if (this.psm != null) {
            return this.psm.startNewSession(distantSessionID, policy, distantCertificate);
        }
        throw new SecurityNotAvailableException();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#terminateSession(long)
     */
    public void terminateSession(long sessionID) throws SecurityNotAvailableException, IOException {
        if (this.psm != null) {
            this.psm.terminateSession(sessionID);
        }
        throw new SecurityNotAvailableException();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObject#getObjectProxy()
     */
    @SuppressWarnings("unchecked")
    public T getObjectProxy() {
        try {
            T reifiedObjectStub = (T) createStubObject();
            if (adapterClass != null) {
                Constructor<Adapter<T>> myConstructor = adapterClass.getConstructor(new Class[] {});// Class                        .forName(this.className)  });
                Adapter<T> ad = myConstructor.newInstance();
                ad.setTargetAndCallConstruct((T) target);
                ad.setTarget(reifiedObjectStub);
                return ad.getAs();
            } else {
                return reifiedObjectStub;
            }
        } catch (Exception e) {
            LOGGER_RO.debug("cannot construct the remote object's proxy", e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObject#getObjectProxy(org.objectweb.proactive.core.remoteobject.RemoteRemoteObject)
     */
    @SuppressWarnings("unchecked")
    public T getObjectProxy(RemoteRemoteObject rro) throws ProActiveException {
        try {
            T reifiedObjectStub = (T) createStubObject();
            ((StubObject) reifiedObjectStub).setProxy(new SynchronousProxy(null, new Object[] { rro }));

            if (adapterClass != null) {

                Class<?>[] classArray = new Class[] { Class.forName(this.className) };
                Constructor<Adapter<T>> myConstructor = adapterClass.getConstructor(classArray);
                Adapter<T> ad = myConstructor.newInstance(reifiedObjectStub);
                return ad.getAs();
            } else {
                return reifiedObjectStub;
            }
        } catch (Exception e) {
            LOGGER_RO.debug("cannot construct the remote object's proxy", e);

        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObject#getClassName()
     */
    public String getClassName() {
        return this.className;
    }

    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObject#getProxyName()
     */
    public String getProxyName() {
        return this.proxyClassName;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObject#getTargetClass()
     */
    public Class<?> getTargetClass() {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObject#getAdapterClass()
     */
    public Class<Adapter<T>> getAdapterClass() {
        if (adapterClass != null) {
            return adapterClass;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getProActiveSecurityManager(org.objectweb.proactive.core.security.securityentity.Entity)
     */
    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
            throws SecurityNotAvailableException, AccessControlException {
        if (this.psm == null) {
            throw new SecurityNotAvailableException();
        }
        return this.psm.getProActiveSecurityManager(user);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#setProActiveSecurityManager(org.objectweb.proactive.core.security.securityentity.Entity, org.objectweb.proactive.core.security.PolicyServer)
     */
    public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
            throws SecurityNotAvailableException, AccessControlException {
        if (this.psm == null) {
            throw new SecurityNotAvailableException();
        }
        this.psm.setProActiveSecurityManager(user, policyServer);
    }

    @SuppressWarnings("unchecked")
    public Adapter<T> getAdapter() {

        if (adapterClass != null) {
            Constructor myConstructor;
            try {

                T reifiedObjectStub = (T) createStubObject();
                myConstructor = adapterClass.getClass().getConstructor(
                        new Class[] { Class.forName(this.className) });
                Adapter<T> ad = (Adapter<T>) myConstructor.newInstance(reifiedObjectStub);
                //            adapter.setAdapterAndCallConstruct(reifiedObjectStub);
                return ad;

            } catch (Exception e) {
                LOGGER_RO.debug("cannot construct the remote object's stub", e);
            }
        }
        return null;
    }

    protected Object createStubObject() throws ClassNotFoundException {
        try {
            return MOP.turnReified(this.className, SynchronousProxy.class.getName(), new Object[] { null,
                    new Object[] { this } }, target, new Class[] {});

        } catch (MOPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public RemoteObjectExposer<T> getRemoteObjectExposer() {
        return this.roe;
    }

    public void setRemoteObjectExposer(RemoteObjectExposer<T> roe) {
        this.roe = roe;
    }
}
