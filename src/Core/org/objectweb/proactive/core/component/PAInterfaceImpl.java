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
package org.objectweb.proactive.core.component;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.Type;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.identity.PAComponent;


/**
 * Abstract implementation of the {@link Interface} interface of the Fractal API.
 * <p>
 * As functional interfaces are specified for each component, they are generated
 * at instantiation time (bytecode generation), by subclassing this class.
 *
 * @author The ProActive Team
 */
public abstract class PAInterfaceImpl implements PAInterface, Serializable {
    private Component owner;
    private String name;
    private Type type;
    private boolean isInternal;

    public PAInterfaceImpl() {
    }

    /*
     * @see org.objectweb.fractal.api.Interface#getFcItfOwner()
     */
    public Component getFcItfOwner() {
        return owner;
    }

    /*
     * @see org.objectweb.fractal.api.Interface#getFcItfName()
     */
    public String getFcItfName() {
        return name;
    }

    /*
     * @see org.objectweb.fractal.api.Interface#getFcItfType()
     */
    public Type getFcItfType() {
        return type;
    }

    /*
     * @see org.objectweb.fractal.api.Interface#isFcInternalItf()
     */
    public boolean isFcInternalItf() {
        return isInternal;
    }

    // The four following setters are only used once after the interface generation

    /**
     * Sets the isInternal.
     * @param isInternal The isInternal to set
     */
    public void setFcIsInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setFcItfName(String name) {
        this.name = name;
    }

    /**
     * Sets the owner.
     * @param owner The owner to set
     */
    public void setFcItfOwner(Component owner) {
        this.owner = owner;
    }

    /**
     * Sets the type.
     *
     * @param type The type to set
     */
    public void setFcType(Type type) {
        this.type = type;
    }

    /**
     *
     * @see org.objectweb.proactive.core.component.PAInterface#getFcItfImpl()
     */
    public abstract Object getFcItfImpl();

    /**
     *
     * @see org.objectweb.proactive.core.component.PAInterface#setFcItfImpl(java.lang.Object)
     */
    public abstract void setFcItfImpl(final Object impl);

    @Override
    public boolean equals(Object anObject) {
        if (Interface.class.isAssignableFrom(anObject.getClass())) {

            Interface itf = (Interface) anObject;
            boolean nameEquality = itf.getFcItfName().equals(name);

            // Are the two itf belong to the same component?
            UniqueID objectID = ((PAComponent) itf.getFcItfOwner()).getID();
            UniqueID thisID = ((PAComponent) owner).getID();
            boolean ownerEquality = objectID.equals(thisID);

            return nameEquality && ownerEquality;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + owner.hashCode();
    }

    @Override
    public String toString() {
        String string = "name : " + getFcItfName() + "\n" + //            "componentIdentity : " + getFcItfOwner() + "\n" + "type : " +
            getFcItfType() + "\n" + "isInternal : " + isFcInternalItf() + "\n";
        return string;
    }
}
