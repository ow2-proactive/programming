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
package functionalTests.stub.stubinterface;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;


public class ProxyOne implements org.objectweb.proactive.core.mop.Proxy {
    protected Object target;

    public ProxyOne(ConstructorCall constructorCall, Object[] parameters) {
        try {
            this.target = constructorCall.execute();
        } catch (Exception e) {
            e.printStackTrace();
            this.target = null;
        }
    }

    public Object reify(MethodCall c) throws InvocationTargetException, IllegalAccessException {
        try {
            Object o = c.execute(target);
            return o;
        } catch (MethodCallExecutionFailedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
