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
package functionalTests.activeobject.futurecallbacks;

import java.util.concurrent.Future;

import org.objectweb.proactive.api.PAEventProgramming;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.MutableInteger;


public class A {
    private A brother;

    public static int counter = 0;

    public A() {
    }

    public void myCallback(Future<MutableInteger> f) throws Exception {
        MutableInteger i = f.get();
        i.getValue();
        synchronized (A.class) {
            A.counter++;
            A.class.notifyAll();
        }
    }

    public void start() throws NoSuchMethodException {
        MutableInteger slow = this.brother.slow();
        PAEventProgramming.addActionOnFuture(slow, "myCallback");
        MutableInteger fast = this.brother.fast();
        PAFuture.waitFor(fast);
        PAEventProgramming.addActionOnFuture(fast, "myCallback");
    }

    public MutableInteger slow() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new MutableInteger();
    }

    public MutableInteger fast() {
        return new MutableInteger();
    }

    public void giveBrother(A a) {
        this.brother = a;
    }
}
