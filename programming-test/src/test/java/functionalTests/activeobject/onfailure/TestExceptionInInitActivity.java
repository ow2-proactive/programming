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
package functionalTests.activeobject.onfailure;

import static junit.framework.Assert.assertTrue;

import org.apache.log4j.Level;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedException;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.body.exceptions.InactiveBodyException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import functionalTests.FunctionalTest;


public class TestExceptionInInitActivity extends FunctionalTest {

    static {
        // Disable future monitoring
        CentralPAPropertyRepository.PA_FUTUREMONITORING_TTM.setValue(0);
        ProActiveLogger.getLogger(Loggers.BODY).setLevel(Level.DEBUG);
        ProActiveLogger.getLogger(Loggers.PAPROXY).setLevel(Level.DEBUG);
        ProActiveLogger.getLogger(Loggers.REQUESTS).setLevel(Level.DEBUG);
        ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).setLevel(Level.DEBUG);
        //  ProActiveLogger.getLogger(Loggers.CORE).setLevel(Level.DEBUG);
    }

    @Test
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        ExceptionInInitActivityAO ao = PAActiveObject.newActive(ExceptionInInitActivityAO.class,
                new Object[] {});
        Thread.sleep(2000);
        boolean exception = false;
        // Should not be executed (or at least a Runtime Exception must been thrown by ao.getTrue())
        try {
            ao.getTrue();
        } catch (RuntimeException e) {
            e.printStackTrace();
            assertTrue(e instanceof BodyTerminatedException);
            exception = true;
        }
        assertTrue(exception);
        exception = false;

        CentralPAPropertyRepository.PA_FUTUREMONITORING_TTM.setValue(21000);
        ao = PAActiveObject.newActive(ExceptionInInitActivityAO.class, new Object[] {});

        // Should not be executed (or at least a Runtime Exception must been thrown by ao.getTrue())
        try {
            ao.getTrue();
        } catch (RuntimeException e) {
            e.printStackTrace();
            assertTrue((e instanceof FutureMonitoringPingFailureException) ||
                (e instanceof BodyTerminatedException) || (e instanceof InactiveBodyException));
            exception = true;
        }
        assertTrue(exception);

    }
}
