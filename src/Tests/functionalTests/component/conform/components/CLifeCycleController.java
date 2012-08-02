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
package functionalTests.component.conform.components;

import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.component.body.ComponentInitActive;


public class CLifeCycleController extends C implements ComponentInitActive, LifeCycleController,
        StateAccessor {

    public static final String CUSTOM_STARTED = "CUSTOM_STARTED";

    public static final String CUSTOM_STOPPED = "CUSTOM_STOPPED";

    private String state = CUSTOM_STOPPED;

    public void initComponentActivity(Body body) {
        PAActiveObject.setImmediateService("getFcCustomState");
    }

    /*
     * In the ProActive/GCM implementation, as in Julia, if the user redefines the method getFcState, this one
     * is ignored.
     * The default implementation of getFcState, provided in PAGCMLifeCycleControllerImpl#getFcState, is ALWAYS
     * used.
     */
    public String getFcState() {
        return this.state;
    }

    public void startFc() throws IllegalLifeCycleException {
        this.state = CUSTOM_STARTED;
    }

    public void stopFc() throws IllegalLifeCycleException {
        this.state = CUSTOM_STOPPED;
    }

    public String getFcCustomState() {
        return this.state;
    }
}
