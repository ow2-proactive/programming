/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
