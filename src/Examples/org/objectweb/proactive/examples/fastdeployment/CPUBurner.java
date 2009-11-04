/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.fastdeployment;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.Cache;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class CPUBurner implements Serializable, InitActive {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    private int id;
    private Manager manager;

    public CPUBurner() {
        // No-args empty constructor
    }

    public CPUBurner(IntWrapper id, Manager manager) {
        this.id = id.intValue();
        this.manager = manager;
    }

    public void compute(LongWrapper l) {
        long val = l.longValue();
        for (long i = 0; i < val; i++) {
            // Does nothing but eats some CPU time
        }

        manager.resultAvailable(new Result(id, null));
    }

    public void initActivity(Body body) {
        PAActiveObject.setImmediateService("getId");
    }

    @Cache
    public IntWrapper getId() {
        return new IntWrapper(id);
    }
}
