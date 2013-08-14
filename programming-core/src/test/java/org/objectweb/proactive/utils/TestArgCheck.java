/*
 * ################################################################
 *
 * Copyright (C) 1997-2012 ActiveEon SAS. All rights reserved.
 * Redistribution, reproduction, or copying to any other server or location of this code
 * is expressly prohibited by law.
 * It may only be obtained and installed by ActiveEon or its commercial partners.
 *
 * ################################################################
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
