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
package org.objectweb.proactive.core.component.adl;

import java.util.HashMap;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;


/**
 * A ProActive factory customizing the Fractal ADL.
 *
 * @author The ProActive Team
 */
public class FactoryFactory {
    public final static String PROACTIVE_FACTORY = "org.objectweb.proactive.core.component.adl.PAFactory";
    public final static String PROACTIVE_BACKEND = "org.objectweb.proactive.core.component.adl.PACompiler";
    public final static String PROACTIVE_NFFACTORY = "org.objectweb.proactive.core.component.adl.PANFFactory";
    public final static String PROACTIVE_NFBACKEND = "org.objectweb.proactive.core.component.adl.PANFCompiler";
    public final static String PROACTIVE_DEBUG_FACTORY = "org.objectweb.proactive.core.component.adl.PADebugFactory";

    private FactoryFactory() {
    }

    /**
     * Returns a factory for the GCM ADL
     *
     * @see org.objectweb.fractal.adl.FactoryFactory#getFactory(java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public static Factory getFactory() throws ADLException {
        return org.objectweb.fractal.adl.FactoryFactory.getFactory(PROACTIVE_FACTORY, PROACTIVE_BACKEND,
                new HashMap());
    }

    /**
     * Returns a factory that creates NF components
     *
     * @see org.objectweb.fractal.adl.FactoryFactory#getFactory(java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public static Factory getNFFactory() throws ADLException {
        return org.objectweb.fractal.adl.FactoryFactory.getFactory(PROACTIVE_NFFACTORY, PROACTIVE_NFBACKEND,
                new HashMap());
    }

    /**
     * Returns a factory for the GCM ADL that prints debugging information
     *
     * @see org.objectweb.fractal.adl.FactoryFactory#getFactory(java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public static Factory getDebugFactory() throws ADLException {
        return org.objectweb.fractal.adl.FactoryFactory.getFactory(PROACTIVE_DEBUG_FACTORY,
                PROACTIVE_BACKEND, new HashMap());
    }

}
