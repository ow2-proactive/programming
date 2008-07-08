/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.interfaces.MemoryFactory;
import umontreal.iro.lecuyer.rng.BasicRandomStreamFactory;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStreamFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * MCMemoryFactory
 *
 * @author The ProActive Team
 */
public class MCMemoryFactory implements MemoryFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 40L;

    private Class prngClass;

    private RandomStreamFactory prngfactory = null;

    /**
     * Creates memory for newly created workers, the memory will contain the provided class as a Random Stream or a MRG32k3a Random Stream if the agrument is null
     */
    public MCMemoryFactory(Class randomStreamClass) {
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
        HashMap map = new HashMap(1);
        map.put("rng", prngfactory.newInstance());
        return map;
    }
}
