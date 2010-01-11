/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package functionalTests.group.barrier;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.api.PASPMD;


//@snippet-start spmd_typical_class
public class A implements Active, java.io.Serializable {

    private String name;
    private int fooCounter = 0;
    private int barCounter = 0;
    private int geeCounter = 0;
    private String errors = "";

    public A() {
    }

    //@snippet-break spmd_typical_class

    public A(String s) {
        this.name = s;
    }

    public String getErrors() {
        return this.errors;
    }

    public void foo() {
        this.fooCounter++;
    }

    public void bar() {
        if (this.fooCounter != 3) {
            this.errors += "'bar' invoked before all 'foo'\n";
        }
        this.barCounter++;
    }

    public void gee() {
        if (this.barCounter != 3) {
            this.errors += "'gee' invoked before all 'bar'\n";
        }
        if (this.fooCounter != 3) {
            this.errors += "'gee' invoked before all 'foo'\n";
        }
        this.geeCounter++;
    }

    public void waitFewSecondes() {
        long n = 0;
        if ("Agent0".equals(this.name)) {
            n = 0;
        } else if ("Agent1".equals(this.name)) {
            n = 1000;
        } else if ("Agent2".equals(this.name)) {
            n = 2000;
        }
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            System.err.println("** InterruptedException **");
        }
    }

    public void start() {
        A myspmdgroup = (A) PASPMD.getSPMDGroup();
        this.waitFewSecondes();
        //@snippet-start spmd_total_barrier_call
        myspmdgroup.foo();
        PASPMD.totalBarrier("'1'");
        myspmdgroup.bar();
        PASPMD.totalBarrier("'2'");
        myspmdgroup.gee();
        //@snippet-end spmd_total_barrier_call
    }
    //@snippet-resume spmd_typical_class
}
//@snippet-end spmd_typical_class
