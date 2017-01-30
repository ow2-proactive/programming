/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


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

    protected RemoteObjectExposer<T> roe;

    protected String name;

    public RemoteObjectImpl(String name, String className, T target) {
        this(name, className, target, null);
    }

    public RemoteObjectImpl(String name, String className, T target, Class<Adapter<T>> adapter) {
        this.target = target;
        this.name = name;
        this.className = className;
        this.proxyClassName = SynchronousProxy.class.getName();
        //        this.adapter =  adapter.getConstructor(Object.class).newInstance(target);
        this.adapterClass = adapter;
    }

    public Reply receiveMessage(Request message) throws ProActiveException {
        try {
            Object o;
            if (message instanceof RemoteObjectRequest) {
                o = ((RemoteObjectRequest) message).execute(this);
            } else {
                o = message.getMethodCall().execute(this.target);
            }

            return new SynchronousReplyImpl(new MethodCallResult(o, null));
        } catch (MethodCallExecutionFailedException e) {
            throw new ProActiveException(e);
        } catch (InvocationTargetException e) {
            return new SynchronousReplyImpl(new MethodCallResult(null, e.getCause()));
        }
    }

    /*
     * (non-Javadoc)
     * 
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.objectweb.proactive.core.remoteobject.RemoteObject#getObjectProxy(org.objectweb.proactive
     * .core.remoteobject.RemoteRemoteObject)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.core.remoteobject.RemoteObject#getClassName()
     */
    public String getClassName() {
        return this.className;
    }

    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
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

    /*
     * (non-Javadoc)
     * 
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

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.core.remoteobject.RemoteObject#getAdapterClass()
     */
    public Class<Adapter<T>> getAdapterClass() {
        if (adapterClass != null) {
            return adapterClass;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Adapter<T> getAdapter() {

        if (adapterClass != null) {
            Constructor myConstructor;
            try {

                T reifiedObjectStub = (T) createStubObject();
                myConstructor = adapterClass.getClass().getConstructor(new Class[] { Class.forName(this.className) });
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
            return MOP.turnReified(this.className,
                                   SynchronousProxy.class.getName(),
                                   new Object[] { null, new Object[] { this } },
                                   target,
                                   new Class[] {});

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
