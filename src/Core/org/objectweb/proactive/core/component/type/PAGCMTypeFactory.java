/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
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
     * @param name the name of interfaces of this type (see {@link
     *       InterfaceType#getFcItfName getFcItfName}).
     * @param signature signatures of the methods of interfaces of this type. In
     *       Java this "signature" is the fully qualified name of a Java interface
     *       corresponding to these method signatures.
     * @param isClient <tt>true</tt> if component interfaces of this type are
     *      client interfaces.
     * @param isOptional <tt>true</tt> if component interfaces of this type are
     *      optional interfaces.
     * @param cardinality see { @link PAInterfaceType#getFcCardinality() }
     * for a description of cardinalities
     * @param isInternal boolean value, indicating whether the interface is internal
     * @return an interface type initialized with the given values.
     * @throws InstantiationException if the interface type cannot be created.
     */
    public InterfaceType createGCMItfType(String name, String signature, boolean isClient,
            boolean isOptional, String cardinality, boolean isInternal) throws InstantiationException;
}
