/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
package functionalTests.activeobject.stack;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * AOStack
 *
 * @author The ProActive Team
 */
public class AOStack {

    public AOStack() {

    }

    public boolean throwExceptionSync1() {
        throw new RuntimeException("Expected Exception");
    }

    public boolean throwExceptionSync2() {
        return createAndReturnAO().throwExceptionSync1();
    }

    public boolean throwExceptionSync3() {
        return createAndReturnAO().throwExceptionSync2();
    }

    public boolean throwExceptionSync4() {
        return createAndReturnAO().throwExceptionSync3();
    }

    public BooleanWrapper throwExceptionAsync1() {
        throw new RuntimeException("Expected Exception");
    }

    public BooleanWrapper throwExceptionAsync2() {
        return createAndReturnAO().throwExceptionAsync1();
    }

    public BooleanWrapper throwExceptionAsync3() {
        return createAndReturnAO().throwExceptionAsync2();
    }

    public BooleanWrapper throwExceptionAsync4() {
        return createAndReturnAO().throwExceptionAsync3();
    }

    private AOStack createAndReturnAO() {
        AOStack ao = null;
        try {
            ao = PAActiveObject.newActive(AOStack.class, new Object[0]);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        return ao;
    }

}
