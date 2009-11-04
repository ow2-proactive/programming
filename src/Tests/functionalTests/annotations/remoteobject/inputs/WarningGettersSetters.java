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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.annotations.remoteobject.inputs;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.annotation.RemoteObject;


@RemoteObject
public class WarningGettersSetters implements Serializable {

    // WARNING (no accessors)
    public int error;

    // WARNING (bad accessors name)
    public int _counter;

    public void setCounter(IntWrapper counter) {
        _counter = counter.intValue();
    }

    public IntWrapper getCounter() {
        return new IntWrapper(_counter);
    }

    // WARNING (bad accessors name)
    public String name;

    public StringWrapper getname() {
        return new StringWrapper(name);
    }

    public void setName(String name) {
    }

    // OK
    String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    // OK
    String _test;

    public String get_test() {
        return _test;
    }

    public void set_test(String _test) {
        this._test = _test;
    }

    // OK (non public)
    int test2;

    // OK (non public)
    private int test3;

    // WARNING (inly getter)
    public int test4;

    public int getTest4() {
        return test4;
    }

    // WARNING (inly setter)
    public int test5;

    public void setTest5(int test5) {
        this.test5 = test5;
    }

    // OK (final field)
    final Object o = new Object();

    // OK - P2PAcquaintanceManager inspired
    private int NOA;

    public int getMaxNOA() {
        return NOA;
    }

    public void setMaxNOA(int noa) {
        NOA = noa;
    }

    public int getNOAMax() {
        return NOA;
    }

    public void setNOAMax(int noa) {
        NOA = noa;
    }

    public int getMaxNOAMinPayne() {
        return NOA;
    }

    public void setMaxPayneNOAMin(int noa) {
        NOA = noa;
    }
}
