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
package org.objectweb.proactive.examples.fibonacci;

import java.io.Serializable;
import java.math.BigInteger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * @author The ProActive Team
 *
 */
@ActiveObject
public class Cons1 implements Serializable, InitActive, RunActive {
    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    private Add add;
    private Cons2 cons2;
    private BigInteger fibN;

    //Empty no arg constructor
    public Cons1() {
    }

    /**
     * @param add The add to set.
     */
    public void setAdd(Add add) {
        this.add = add;
    }

    /**
     * @param cons2 The cons2 to set.
     */
    public void setCons2(Cons2 cons2) {
        this.cons2 = cons2;
    }

    /**
     * @param fibN The fibN to set.
     */
    public void setFibN(BigInteger fibN) {
        this.fibN = fibN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        Service service = new Service(body);
        service.blockingServeOldest("setAdd");
        service.blockingServeOldest("setCons2");
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        add.setFibN_1(BigInteger.ONE); // starting with 1 
        cons2.setFibN_1(BigInteger.ONE); // starting with 1

        while (body.isActive()) {
            service.blockingServeOldest("setFibN");
            add.setFibN_1(fibN);
            cons2.setFibN_1(fibN);
        }
    }
}
