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
package org.objectweb.proactive.extra.montecarlo.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extensions.masterworker.interfaces.MemoryFactory;

import umontreal.iro.lecuyer.rng.BasicRandomStreamFactory;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStreamFactory;


/**
 * MCMemoryFactory
 *
 * @author The ProActive Team
 */
public class MCMemoryFactory implements MemoryFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;

    private Class<?> prngClass;

    private RandomStreamFactory prngfactory = null;

    /**
     * Creates memory for newly created workers, the memory will contain the provided class as a Random Stream or a MRG32k3a Random Stream if the agrument is null
     */
    public MCMemoryFactory(Class<?> randomStreamClass) {
        if (randomStreamClass == null) {
            prngClass = MRG32k3a.class;
        } else {
            prngClass = randomStreamClass;
        }
    }

    public Map<String, Serializable> newMemoryInstance() {
        if (prngfactory == null) {
            prngfactory = new BasicRandomStreamFactory(prngClass);
        }
        HashMap<String, Serializable> map = new HashMap<String, Serializable>(1);
        map.put("rng", (Serializable) prngfactory.newInstance());
        return map;
    }
}
