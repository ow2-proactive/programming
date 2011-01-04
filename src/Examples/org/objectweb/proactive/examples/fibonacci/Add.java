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
package org.objectweb.proactive.examples.fibonacci;

import java.io.Serializable;
import java.math.BigInteger;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * @author The ProActive Team
 *
 */
@ActiveObject
public class Add implements Serializable, InitActive, RunActive {
    private Cons1 cons1;
    private BigInteger fibN_1;
    private BigInteger fibN_2;

    //Empty noArg constructor
    public Add() {
    }

    public void initActivity(Body body) {
        Service service = new Service(body);
        service.blockingServeOldest("setCons1");
    }

    /**
     * @param cons1 The cons1 to set.
     */
    public void setCons1(Cons1 cons1) {
        this.cons1 = cons1;
    }

    /**
     * @param fibN_1 The fibN_1 to set.
     */
    public void setFibN_1(BigInteger fibN_1) {
        this.fibN_1 = fibN_1;
    }

    /**
     * @param fibN_2 The fibN_2 to set.
     */
    public void setFibN_2(BigInteger fibN_2) {
        this.fibN_2 = fibN_2;
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            service.blockingServeOldest("setFibN_1");
            service.blockingServeOldest("setFibN_2");
            cons1.setFibN(fibN_1.add(fibN_2));
        }
    }

    public static void main(String[] args) {
        try {
            Add add = PAActiveObject.newActive(Add.class, null);
            Cons1 cons1 = PAActiveObject.newActive(Cons1.class, null);
            Cons2 cons2 = PAActiveObject.newActive(Cons2.class, null);
            add.setCons1(cons1);
            cons1.setAdd(add);
            cons1.setCons2(cons2);
            cons2.setAdd(add);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }
}
