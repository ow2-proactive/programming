/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.activeobject.implicitgetstubonthis;

import java.io.Serializable;

import org.objectweb.proactive.core.mop.StubObject;


public class B implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    int toto;
    private A a;

    public B() {

        new C().init(this);
        toto = 1;
    }

    public B(A a) {
        this.setA(a);
    }

    public int getToto() {
        return toto;
    }

    public boolean takeA(A a) {
        return a instanceof StubObject;

    }

    public static void main(String[] args) {
        new B();
    }

    public void setA(A a) {
        this.a = a;
    }

    public A getA() {
        return a;
    }
}
