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
package org.objectweb.proactive.core.mop;

import java.lang.reflect.InvocationTargetException;


/**
 * Fakes a constructor call by returning an already-existing object as if
 * it were the result of the reflection of this ConstructorCall object
 */
class FakeConstructorCall implements ConstructorCall, java.io.Serializable {
    private Object target;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     *
     */
    public FakeConstructorCall(Object target) {
        this.target = target;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements ConstructorCall -----------------------------------------------
    //

    /**
     * Make a deep copy of all arguments of the constructor
     * Do nothing as we don't want to copy the object if used locally
     */
    public void makeDeepCopyOfArguments() throws java.io.IOException {
    }

    /**
     * Return the name of the target class that constructor is for
     */
    public String getTargetClassName() {
        return target.getClass().getName();
    }

    /**
     * Performs the object construction that is reified vy this object
     * @throws InvocationTargetException
     * @throws ConstructorCallExecutionFailedException
     */
    public Object execute() throws InvocationTargetException, ConstructorCallExecutionFailedException {
        return target;
    }

    public Object[] getEffectiveArguments() {
        return null;
    }

    public void setEffectiveArguments(Object[] effectiveArguments) {
    }
}
