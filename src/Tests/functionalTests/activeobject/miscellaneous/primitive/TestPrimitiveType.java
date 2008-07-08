/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
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
        PAActiveObject.newActive(Boolean.class.getName(), new Object[] {});
    }

    @Test
    public void testByte() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Byte.class.getName(), new Object[] {});
    }

    @Test
    public void testChar() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Char.class.getName(), new Object[] {});
    }

    @Test
    public void testShort() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Short.class.getName(), new Object[] {});
    }

    @Test
    public void testInt() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Int.class.getName(), new Object[] {});
    }

    @Test
    public void testLong() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Long.class.getName(), new Object[] {});
    }

    @Test
    public void testFloat() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Float.class.getName(), new Object[] {});
    }

    @Test
    public void testDouble() throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(Double.class.getName(), new Object[] {});
    }

    static public class Boolean implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 40L;

        public Boolean() {
        }

        public void serve(boolean[] buf) {
        }
    }

    static public class Byte implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 40L;

        public Byte() {
        }

        public void serve(byte[] buf) {
        }
    }

    static public class Char implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 40L;

        public Char() {
        }

        public void serve(char[] buf) {
        }
    }

    static public class Short implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 40L;

        public Short() {
        }

        public void serve(short[] buf) {
        }
    }

    static public class Int implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 40L;

        public Int() {
        }

        public void serve(int[] buf) {
        }
    }

    static public class Long implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 40L;

        public Long() {
        }

        public void serve(long[] buf) {
        }
    }

    static public class Float implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 40L;

        public Float() {
        }

        public void serve(float[] buf) {
        }
    }

    static public class Double implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 40L;

        public Double() {
        }

        public void serve(double[] buf) {
        }
    }

}
