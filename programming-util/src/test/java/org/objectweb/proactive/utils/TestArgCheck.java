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
package org.objectweb.proactive.utils;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;


/**
 * @since 5.1.0
 */
public class TestArgCheck {

    @Test
    public void testRequireNonNull() {
        ArgCheck.requireNonNull(new Object());
        try {
            ArgCheck.requireNonNull(null);
            Assert.fail("Should have thrown an NPE");
        } catch (NullPointerException e) {
        }

        String msg = "msg";
        ArgCheck.requireNonNull(new Object(), msg);
        try {
            ArgCheck.requireNonNull(null, msg);
            Assert.fail("Should have thrown an NPE");
        } catch (NullPointerException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testRequirePositiveLong() {
        ArgCheck.requirePostive(1L);
        ArgCheck.requirePostive(0L);
        try {
            ArgCheck.requirePostive(-1L);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
        }

        String msg = "msg";
        ArgCheck.requirePostive(1L, msg);
        ArgCheck.requirePostive(0L, msg);
        try {
            ArgCheck.requirePostive(-1L, msg);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testRequireStrictlyPositiveLong() {
        ArgCheck.requireStrictlyPostive(1L);
        try {
            ArgCheck.requireStrictlyPostive(0L);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
        }
        try {
            ArgCheck.requireStrictlyPostive(-1L);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
        }

        String msg = "msg";
        ArgCheck.requireStrictlyPostive(1L, msg);
        try {
            ArgCheck.requireStrictlyPostive(0L, msg);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
        try {
            ArgCheck.requireStrictlyPostive(-1L, msg);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testRequirePositiveInteger() {
        ArgCheck.requirePostive(1);
        ArgCheck.requirePostive(0);
        try {
            ArgCheck.requirePostive(-1);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
        }

        String msg = "msg";
        ArgCheck.requirePostive(1, msg);
        ArgCheck.requirePostive(0, msg);
        try {
            ArgCheck.requirePostive(-1, msg);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testRequireStrictlyPositiveInteger() {
        ArgCheck.requireStrictlyPostive(1);
        try {
            ArgCheck.requireStrictlyPostive(0);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
        }
        try {
            ArgCheck.requireStrictlyPostive(-1);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
        }

        String msg = "msg";
        ArgCheck.requireStrictlyPostive(1, msg);
        try {
            ArgCheck.requireStrictlyPostive(0, msg);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
        try {
            ArgCheck.requireStrictlyPostive(-1, msg);
            Assert.fail("Should have thrown an NPE");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testSetWithoutNullValue() {
        // HashSet permit null value
        HashSet<String> hashset;

        hashset = new HashSet<String>();
        ArgCheck.setWithoutNull(hashset);
        hashset.add("toto");
        ArgCheck.setWithoutNull(hashset);
        hashset.add(null);
        try {
            ArgCheck.setWithoutNull(hashset);
            Assert.fail("Should have thrown an NPE");
        } catch (NullPointerException e) {
        }

        String msg = "msg";
        hashset = new HashSet<String>();
        ArgCheck.setWithoutNull(hashset, msg);
        hashset.add("toto");
        ArgCheck.setWithoutNull(hashset, msg);
        hashset.add(null);
        try {
            ArgCheck.setWithoutNull(hashset, msg);
            Assert.fail("Should have thrown an NPE");
        } catch (NullPointerException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        // should also check the behavior with a set that does not allow
        // the null value but wasn't able to find one in the JDK
    }
}
