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
package org.objectweb.proactive.core.component.type;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * The PAGCMTypeFactory extends the {@link GCMTypeFactory} to support the internal parameter.
 *
 * @author The ProActive Team
 * @see GCMTypeFactory
 */
@PublicAPI
public interface PAGCMTypeFactory extends GCMTypeFactory {
    public static final boolean INTERNAL = true;
    public static final boolean EXTERNAL = false;

    /**
     * Creates an interface type.
     *
     * @param name The name of interfaces of this type (see {@link
     *       InterfaceType#getFcItfName getFcItfName}).
     * @param signature Signatures of the methods of interfaces of this type. In
     *       Java this "signature" is the fully qualified name of a Java interface
     *       corresponding to these method signatures.
     * @param isClient <tt>true</tt> if component interfaces of this type are
     *      client interfaces.
     * @param isOptional <tt>true</tt> if component interfaces of this type are
     *      optional interfaces.
     * @param cardinality See {@link PAGCMInterfaceType#getGCMCardinality()}
     * for a description of cardinalities
     * @param isInternal Boolean value, indicating whether the interface is internal
     * @return An interface type initialized with the given values.
     * @throws InstantiationException If the interface type cannot be created.
     */
    public InterfaceType createGCMItfType(String name, String signature, boolean isClient,
            boolean isOptional, String cardinality, boolean isInternal) throws InstantiationException;

    /**
     * Creates a component type.
     *
     * @param fInterfaceTypes The functional interface types of the component type to be
     *      created.
     * @param nfInterfaceTypes The non functional interface types of the component type to be
     *      created.
     * @return A component type whose {@link ComponentType#getFcInterfaceTypes
     *      getFcInterfaceTypes} method returns an array equal to
     *      <tt>fInterfaceTypes</tt> + <tt>nfInterfaceTypes</tt>.
     * @throws InstantiationException if the component type cannot be created.
     */
    public ComponentType createFcType(InterfaceType[] fInterfaceTypes, InterfaceType[] nfInterfaceTypes)
            throws InstantiationException;
}
