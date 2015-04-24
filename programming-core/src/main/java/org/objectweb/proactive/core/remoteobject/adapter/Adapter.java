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
package org.objectweb.proactive.core.remoteobject.adapter;

import java.io.Serializable;

import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * @author The ProActive Team
 * Remote Object Adapter is a mechanism that allow to insert an interception object.
 * Thus it is possible to insert personalized mechanisms within the remote objects like a cache mechanism
 * @param <T>
 */

public abstract class Adapter<T> implements Serializable, StubObject, Cloneable {

    private static final long serialVersionUID = 62L;

    /**
     * the generated stub
     */
    protected T target;

    public Adapter() {
    }

    /**
     * @param target the generated stub
     */
    public Adapter(T target) {
        this.target = target;
        construct();
    }

    /**
     * a method that allows to change the default target of the adapter.
     * @param target the new target of this adapter
     */
    public void setTarget(T target) {
        this.target = target;
    }

    /**
     * a method that allows to change the default target of the adapter.
     * Setting a new adapter could invalid some of the treatment done when this adapter has been constructed,
     * that why construct() is called once again.
     * @param target the new target of this adapter
     */
    public void setTargetAndCallConstruct(T target) {
        this.target = target;
        construct();
    }

    /**
     * @return return the current target of this adapter
     */
    public T getTarget() {
        return target;
    }

    /**
     * a method called during the constructor call.
     * If some treatment has to be done during the constructor call, Adapters have to
     * override this method
     */
    protected abstract void construct();

    /**
     * set the proxy to the active object
     */
    public void setProxy(Proxy p) {
        ((StubObject) target).setProxy(p);
    }

    /**
     * return the proxy to the active object
     */
    public Proxy getProxy() {
        return ((StubObject) target).getProxy();
    }

    /**
     * returns this with the type of the generic parameter
     * @return this with the type of the generic parameter
     */
    @SuppressWarnings("unchecked")
    public T getAs() {
        return (T) this;
    }
}
