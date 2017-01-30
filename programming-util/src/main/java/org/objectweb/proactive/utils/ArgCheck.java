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

import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A set of helper methods to perform argument sanitization
 * 
 * Usually when a method is part of a public API it wants to perform argument
 * checking and sanitization. It means that if null is not a valid value for a 
 * given argument the method should throw an NPE as soon as possible instead of 
 * crashing latter.
 * 
 * Unfortunately Java provides no helper methods which leads to a lot of redundant
 * code. Few methods will be added in Java 7 with the Objects class.
 * 
 * @since 0.0.2
 */
@PublicAPI
final public class ArgCheck {

    private ArgCheck() {
        // Avoid ArgCheck from being instanciable 
    }

    /**
     * Checks that the specified object reference is not null.
     * 
     * @param <T>
     *    the type of the reference
     * @param obj
     *    the object reference to check for nullity
     * @return
     *    obj if not null
     * @throws NullPointerException
     *    if obj is null
     */
    static public <T> T requireNonNull(T obj) throws NullPointerException {
        return requireNonNull(obj, "Reference cannot be null");
    }

    /**
     * Checks that the specified object reference is not null and throws a customized NullPointerException if it is. 
     * 
     * This method is designed primarily for doing parameter validation in methods and constructors with 
     * multiple parameters.
     * 
     * @param <T>
     *    the type of the reference
     * @param obj
     *    the object reference to check for nullity
     * @param errMsg
     *    the customized error message
     * @return
     *    obj if not null
     * @throws NullPointerException
     *    if obj is null
     */
    static public <T> T requireNonNull(T obj, String errMsg) throws NullPointerException {
        if (obj == null) {
            throw new NullPointerException(errMsg);
        }

        return obj;
    }

    /**
     * Checks that the specified long is positive.
     * 
     * @param l
     *    the long
     * @return
     *    l if positive
     * @throws IllegalArgumentException
     *    if l if not positive
     */
    static public long requirePostive(long l) throws IllegalArgumentException {
        return requirePostive(l, "long value must be positive");
    }

    /**
     * Checks that the specified long is positive and throws a customized NullPointerException if it is.
     * 
     * This method is designed primarily for doing parameter validation in methods and constructors with 
     * multiple parameters.
     * 
     * @param l
     *    the long
     * @param msg
     *    the customized error message
     * @return
     *    l if positive
     * @throws IllegalArgumentException
     *    if l if not positive
     */
    static public long requirePostive(long l, String msg) throws IllegalArgumentException {
        if (l < 0) {
            throw new IllegalArgumentException(msg);
        }

        return l;
    }

    /**
     * Checks that the specified long is strictly positive.
     * 
     * @param l
     *    the long
     * @return
     *    l if positive
     * @throws IllegalArgumentException
     *    if l if not positive
     */
    static public long requireStrictlyPostive(long l) throws IllegalArgumentException {
        return requireStrictlyPostive(l, "long value must be strictly positive");
    }

    /**
     * Checks that the specified long is positive and throws a customized NullPointerException if it is.
     * 
     * This method is designed primarily for doing parameter validation in methods and constructors with 
     * multiple parameters.
     * 
     * @param l
     *    the long
     * @param msg
     *    the customized error message
     * @return
     *    l if positive
     * @throws IllegalArgumentException
     *    if l if not positive
     */
    static public long requireStrictlyPostive(long l, String msg) throws IllegalArgumentException {
        if (l <= 0) {
            throw new IllegalArgumentException(msg);
        }

        return l;
    }

    /**
     * Checks that the specified integer is positive.
     * 
     * @param i
     *    the integer
     * @return
     *    i if positive
     * @throws IllegalArgumentException
     *    if i if not positive
     */
    static public int requirePostive(int i) throws IllegalArgumentException {
        return requirePostive(i, "integer value must be positive");
    }

    /**
     * Checks that the specified integer is positive and throws a customized IllegalArgumentException if it is.
     * 
     * This method is designed primarily for doing parameter validation in methods and constructors with 
     * multiple parameters.
     * 
     * @param i
     *    the integer
     * @param msg
     *    the customized error message
     * @return
     *    i if positive
     * @throws IllegalArgumentException
     *    if i if not positive
     */
    static public int requirePostive(int i, String msg) throws IllegalArgumentException {
        if (i < 0) {
            throw new IllegalArgumentException(msg);
        }

        return i;
    }

    /**
     * Checks that the specified integer is strictly positive.
     * 
     * @param i
     *    the integer
     * @return
     *    i if positive
     * @throws IllegalArgumentException
     *    if i if not positive
     */
    static public int requireStrictlyPostive(int i) throws IllegalArgumentException {
        return requireStrictlyPostive(i, "long value must be strictly positive");
    }

    /**
     * Checks that the specified integer is positive and throws a customized IllegalArguementException if it is.
     * 
     * This method is designed primarily for doing parameter validation in methods and constructors with 
     * multiple parameters.
     * 
     * @param i
     *    the integer
     * @param msg
     *    the customized error message
     * @return
     *    i if positive
     * @throws IllegalArgumentException
     *    if i if not positive
     */
    static public int requireStrictlyPostive(int i, String msg) throws IllegalArgumentException {
        if (i <= 0) {
            throw new IllegalArgumentException(msg);
        }

        return i;
    }

    /**
     * Checks that a set does not contain a null value.
     * 
     * @param <T>
     *    the type of the reference in the set
     * @param set
     *    the set to be checked for null values
     * @return
     *    set if it does not contains the null value
     * @throws NullPointerException
     *    if set contains null
     */
    static public <T> Set<T> setWithoutNull(Set<T> set) throws NullPointerException {
        return setWithoutNull(set, "Set cannot contains null value");
    }

    /**
     * Checks that a set does not contain a null value and throws a customized NullPointerException if found.
     * 
     * This method is designed primarily for doing parameter validation in methods and constructors with 
     * multiple parameters.
     * @param <T>
     *    the type of the reference in the set
     * @param set
     *    the set to be checked for null values
     * @param msg
     *    the customized error message
     * @return
     *    set if it does not contains the null value
     * @throws NullPointerException
     *    if set contains null
     */
    static public <T> Set<T> setWithoutNull(Set<T> set, String msg) throws NullPointerException {
        final boolean b;
        try {
            b = set.contains(null);
        } catch (NullPointerException e) {
            return set; // Set does not allow null
        }

        if (b) {
            throw new NullPointerException(msg);
        }
        return set;
    }
}
