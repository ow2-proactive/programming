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
package functionalTests.activeobject.miscellaneous.primitive;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


public class TestPrimitiveType extends FunctionalTest {

    @Test
    public void testBoolean() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Boolean.class, new Object[] {});
    }

    @Test
    public void testByte() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Byte.class, new Object[] {});
    }

    @Test
    public void testChar() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Char.class, new Object[] {});
    }

    @Test
    public void testShort() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Short.class, new Object[] {});
    }

    @Test
    public void testInt() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Int.class, new Object[] {});
    }

    @Test
    public void testLong() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Long.class, new Object[] {});
    }

    @Test
    public void testFloat() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Float.class, new Object[] {});
    }

    @Test
    public void testDouble() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Double.class, new Object[] {});
    }

    static public class Boolean implements Serializable {
        public Boolean() {
        }

        public void serve(boolean[] buf) {
        }
    }

    static public class Byte implements Serializable {
        public Byte() {
        }

        public void serve(byte[] buf) {
        }
    }

    static public class Char implements Serializable {
        public Char() {
        }

        public void serve(char[] buf) {
        }
    }

    static public class Short implements Serializable {
        public Short() {
        }

        public void serve(short[] buf) {
        }
    }

    static public class Int implements Serializable {
        public Int() {
        }

        public void serve(int[] buf) {
        }
    }

    static public class Long implements Serializable {
        public Long() {
        }

        public void serve(long[] buf) {
        }
    }

    static public class Float implements Serializable {
        public Float() {
        }

        public void serve(float[] buf) {
        }
    }

    static public class Double implements Serializable {
        public Double() {
        }

        public void serve(double[] buf) {
        }
    }

}
