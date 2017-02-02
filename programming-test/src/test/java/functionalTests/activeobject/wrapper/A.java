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
package functionalTests.activeobject.wrapper;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;
import org.objectweb.proactive.core.util.wrapper.FloatMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.FloatWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.LongMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class A implements RunActive, Serializable {

    /**
     *
     */
    public A() {
    }

    public BooleanMutableWrapper testBooleanMutableWrapper() {
        return new BooleanMutableWrapper(false);
    }

    public DoubleMutableWrapper testDoubleMutableWrapper() {
        return new DoubleMutableWrapper(0);
    }

    public IntMutableWrapper testIntMutableWrapper() {
        return new IntMutableWrapper(0);
    }

    public LongMutableWrapper testLongMutableWrapper() {
        return new LongMutableWrapper(0);
    }

    public StringMutableWrapper testStringMutableWrapper() {
        return new StringMutableWrapper("Alexandre dC is a famous coder <-- do you mean that ? really ?");
    }

    public FloatMutableWrapper testFloatMutableWrapper() {
        return new FloatMutableWrapper(0);
    }

    public BooleanWrapper testBooleanWrapper() {
        return new BooleanWrapper(false);
    }

    public DoubleWrapper testDoubleWrapper() {
        return new DoubleWrapper(0);
    }

    public IntWrapper testIntWrapper() {
        return new IntWrapper(0);
    }

    public LongWrapper testLongWrapper() {
        return new LongWrapper(0);
    }

    public StringWrapper testStringWrapper() {
        return new StringWrapper("Alexandre dC is a famous coder <-- do you mean that ? really ?");
    }

    public FloatWrapper testFloatWrapper() {
        return new FloatWrapper(0);
    }

    public void terminate() {
        PAActiveObject.terminateActiveObject(true);
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        try {
            while (body.isActive()) {
                service.blockingServeOldest("terminate");
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
