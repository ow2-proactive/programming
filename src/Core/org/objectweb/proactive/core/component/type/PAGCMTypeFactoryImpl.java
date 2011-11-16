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

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of {@link PAGCMTypeFactory}. Implements the Singleton pattern.
 *
 * @author The ProActive Team
 */
public class PAGCMTypeFactoryImpl implements PAGCMTypeFactory {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    // SINGLETON implementation
    static private PAGCMTypeFactoryImpl instance = null;

    /**
     * Constructor for PATypeFactoryImpl.
     */
    private PAGCMTypeFactoryImpl() {
        super();
    }

    static public PAGCMTypeFactoryImpl instance() {
        if (instance == null) {
            instance = new PAGCMTypeFactoryImpl();
        }

        return instance;
    }

    /*
     * @see org.objectweb.fractal.api.type.TypeFactory#createFcItfType(String, String, boolean,
     * boolean, boolean)
     */
    public InterfaceType createFcItfType(String name, String signature, boolean isClient, boolean isOptional,
            boolean isCollection) throws InstantiationException {
        return new PAGCMInterfaceTypeImpl(name, signature, isClient, isOptional,
            (isCollection ? GCMTypeFactory.COLLECTION_CARDINALITY : GCMTypeFactory.SINGLETON_CARDINALITY));
    }

    /*
     * @see org.objectweb.fractal.api.type.TypeFactory#createFcType(InterfaceType[])
     */
    public ComponentType createFcType(InterfaceType[] fInterfaceTypes) throws InstantiationException {
        /*
         * Workaround for null component types. AOKell and ProActive/Fractal assumes a component
         * type is non null, whereas Julia envisions situations where this can be the case. To
         * preserve a kind of compatibility, we bypass null component types with empty arrays of
         * interface types.
         */
        if (fInterfaceTypes == null) {
            fInterfaceTypes = new InterfaceType[] {};
        }
        return new PAComponentTypeImpl(fInterfaceTypes);
    }

    /*
     * @see org.objectweb.fractal.api.type.TypeFactory#createFcType(InterfaceType[])
     */
    public ComponentType createFcType(InterfaceType[] fInterfaceTypes, InterfaceType[] nfInterfaceTypes)
            throws InstantiationException {
        /*
         * Workaround for null component types. AOKell and ProActive/Fractal assumes a component
         * type is non null, whereas Julia envisions situations where this can be the case. To
         * preserve a kind of compatibility, we bypass null component types with empty arrays of
         * interface types.
         */
        if (fInterfaceTypes == null) {
            fInterfaceTypes = new InterfaceType[] {};
        }
        if (nfInterfaceTypes == null) {
            nfInterfaceTypes = new InterfaceType[] {};
        }
        return new PAComponentTypeImpl(fInterfaceTypes, nfInterfaceTypes);
    }

    /*
     * @see
     * org.objectweb.proactive.core.component.type.PATypeFactory#createFcItfType(java.lang.String,
     * java.lang.String, boolean, boolean, java.lang.String)
     */
    public InterfaceType createGCMItfType(String name, String signature, boolean isClient,
            boolean isOptional, String cardinality) throws InstantiationException {
        return new PAGCMInterfaceTypeImpl(name, signature, isClient, isOptional, cardinality);
    }

    public InterfaceType createGCMItfType(String name, String signature, boolean isClient,
            boolean isOptional, String cardinality, boolean isInternal) throws InstantiationException {
        return new PAGCMInterfaceTypeImpl(name, signature, isClient, isOptional, cardinality, isInternal);
    }
}
