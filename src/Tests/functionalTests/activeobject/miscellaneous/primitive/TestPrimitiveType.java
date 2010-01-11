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
