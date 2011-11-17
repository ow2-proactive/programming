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
package org.objectweb.proactive.core.component.type;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A ProActive/GCM component type. A ProActive/GCM component type is just a set of two collections of component
 * interface types (the first collection for functional interfaces and the second one for non functional interfaces),
 * which describes the interfaces that components of this type must or may have at runtime.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface PAComponentType extends ComponentType {
    /**
     * Returns the types of all functional and non functional interface types of components of this type.
     *
     * @return The types of the functional and non functional interfaces that components of this type must or may
     * have at runtime.
     */
    public InterfaceType[] getAllFcInterfaceTypes();

    /**
     * Returns an interface type (functional or non functional) of this component type from its name. This method
     * is not strictly necessary, as it can be implemented by using the {@link #getAllFcInterfaceTypes
     * getAllFcInterfaceTypes} method. But it is convenient and can be implemented more efficiently than with the
     * previous method. This is why it is specified here.
     *
     * @param name The name of one of the interface types (functional or non functional) returned by {@link
     *      #getAllFcInterfaceTypes getAllFcInterfaceTypes}.
     * @return The interface type (functional or non functional) of this component type whose name is equal to
     *      the given name (see {@link InterfaceType#getFcItfName getFcItfName}).
     * @throws NoSuchInterfaceException If there is no such interface type (functional or non functional).
     */
    public InterfaceType getAllFcInterfaceType(String name) throws NoSuchInterfaceException;

    /**
     * Returns the types of the non functional interfaces of components of this type.
     *
     * @return The types of the non functional interfaces that components of this type must or
     *      may have at runtime.
     */
    public InterfaceType[] getNfFcInterfaceTypes();

    /**
     * Returns a non functional interface type of this component type from its name. This method
     * is not strictly necessary, as it can be implemented by using the {@link
     * #getNfFcInterfaceTypes getNfFcInterfaceTypes} method. But it is convenient and
     * can be implemented more efficiently than with the previous method. This is
     * why it is specified here.
     *
     * @param name The name of one of the non functional interface types returned by {@link
     *      #getNfFcInterfaceTypes getNfFcInterfaceTypes}.
     * @return The non functional interface type of this component type whose name is equal to
     *      the given name (see {@link InterfaceType#getFcItfName getFcItfName}).
     * @throws NoSuchInterfaceException If there is no such non functional interface type.
     */
    public InterfaceType getNfFcInterfaceType(String name) throws NoSuchInterfaceException;
}
