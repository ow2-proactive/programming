/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.ext.hpc;

import java.lang.reflect.Method;

import org.objectweb.proactive.core.mop.MethodCall;


public class MethodCallDummy extends MethodCall {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    private static MethodCallDummy immediate_mc_dummy = new MethodCallDummy(true);
    private static MethodCallDummy mc_dummy = new MethodCallDummy(false);
    private Method dummyMethod;
    private String methodName;

    private MethodCallDummy(boolean isImmediate) {
        dummyMethod = this.getClass().getDeclaredMethods()[0];
        if (isImmediate) {
            methodName = "_ImmediateMethodCallDummy";
        } else {
            methodName = "_MethodCallDummy";
        }
    }

    public static MethodCallDummy getMethodCallDummy() {
        return mc_dummy;
    }

    public static MethodCallDummy getImmediateMethodCallDummy() {
        return immediate_mc_dummy;
    }

    //
    // --- PUBLIC METHODS OVERRIDING -------------------------------------------
    //  
    /**
     * Returns the name of the call
     * 
     * @return the name of the call
     */
    @Override
    public String getName() {
        return methodName;
    }

    /**
     * A MethodCallExchange is always a one-way call
     */
    @Override
    public boolean isOneWayCall() {
        return true;
    }

    @Override
    public Method getReifiedMethod() {
        return dummyMethod;
    }

    /**
     * Nothing is performed when serving this method call Everything was done at serialization -
     * deserialization time
     * 
     * @param target
     *            this object is not used.
     * @return null
     */
    @Override
    public Object execute(Object target) {
        return null;
    }

    //
    // --- PROTECTED METHODS FOR SERIALIZATION ---------------------------------
    //
    protected void writeTheObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.flush();
    }

    protected void readTheObject(java.io.ObjectInputStream in) throws java.io.IOException,
            ClassNotFoundException {
    }
}
