/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.component.interceptor.nfcomponent;

import org.objectweb.proactive.core.component.interception.Interceptor;
import org.objectweb.proactive.core.mop.MethodCall;

import functionalTests.component.controller.DummyController;


public class InterceptorImpl implements DummyController, Interceptor {
    public static final String COMPONENT_NAME = "interceptor";
    public static final String DUMMY_SERVICES = "dummy-services";
    public static final String INTERCEPTOR_SERVICES = "interceptor-services";
    public static final String BEFORE_INTERCEPTION = " - before-interception - ";
    public static final String AFTER_INTERCEPTION = " - after-interception - ";

    private String dummyValue = null;

    @Override
    public void setDummyValue(String value) {
        this.dummyValue = value;
    }

    @Override
    public String getDummyValue() {
        return this.dummyValue;
    }

    @Override
    public void beforeMethodInvocation(String interfaceName, MethodCall methodCall) {
        setDummyValue(getDummyValue() + BEFORE_INTERCEPTION + interfaceName + "-" + methodCall.getName() +
            " - ");
    }

    @Override
    public void afterMethodInvocation(String interfaceName, MethodCall methodCall, Object result) {
        setDummyValue(getDummyValue() + AFTER_INTERCEPTION + interfaceName + "-" + methodCall.getName() +
            " - ");
    }
}
