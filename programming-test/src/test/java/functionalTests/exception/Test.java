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
package functionalTests.exception;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PAException;

import functionalTests.FunctionalTest;


/**
 * Test exceptions
 * @author The ProActive Team
 * @version 1.0, 25 mars 2005
 * @since ProActive 2.2
 *
 */

public class Test extends FunctionalTest {
    public Test() {
    }

    int counter = 0;

    void good() {
        counter++;
    }

    private void bad() {
        counter = 1000;
        new Exception("Exception error").printStackTrace();
    }

    public void testMechanism(Exc r) throws Exception {
        PAException.tryWithCatch(Exception.class);
        try {
            /* voidRT() */
            r.voidRT();
            PAException.endTryWithCatch();
            bad();
        } catch (Exception e) {
            good();
        } finally {
            PAException.removeTryWithCatch();
        }
        /* futureRT() */
        PAException.tryWithCatch(RuntimeException.class);
        try {
            Exc res = r.futureRT();
            good();
            res.nothing();
            bad();
            PAException.endTryWithCatch();
        } catch (RuntimeException re) {
            good();
        } finally {
            PAException.removeTryWithCatch();
        }

        /* voidExc() */
        PAException.tryWithCatch(Exception.class);
        try {
            r.voidExc();
            good();
            PAException.waitForPotentialException();
            bad();
            PAException.endTryWithCatch();
        } catch (Exception e) {
            if (e.getMessage().startsWith("Test")) {
                good();
            } else {
                bad();
            }
        } finally {
            PAException.removeTryWithCatch();
        }

        /* futureExc() */
        PAException.tryWithCatch(Exception.class);
        try {
            r.futureExc();
            good();
            PAException.endTryWithCatch();
            bad();
        } catch (Exception e) {
            if (e.getMessage().startsWith("Test")) {
                good();
            } else {
                bad();
            }
        } finally {
            PAException.removeTryWithCatch();
        }

        /* futureExc() synchronous */
        try {
            r.futureExc();
        } catch (Exception e) {
            if (e.getMessage().startsWith("Test")) {
                good();
            } else {
                bad();
            }
        }

        PAException.tryWithCatch(Exception.class);
        r.futureExc();
        try {
            PAException.tryWithCatch(Exception.class);
        } catch (Exception e) {
            if (e.getMessage().startsWith("Test")) {
                good();
            } else {
                bad();
            }
        } finally {
            PAException.removeTryWithCatch();
        }

        PAException.tryWithCatch(Exception.class);
        try {
            r.voidExc();
            r.futureExc();
            PAException.endTryWithCatch();
        } catch (Exception e) {
            int size = PAException.getAllExceptions().size();
            if (size == 2) {
                good();
            } else {
                System.out.println("size: " + size);
                bad();
            }
        } finally {
            PAException.removeTryWithCatch();
        }

        assertTrue(counter == 13);
    }

    @org.junit.Test
    public void action() throws Exception {

        /* Server */
        Exc r = org.objectweb.proactive.api.PAActiveObject.newActive(Exc.class, null);

        /* Client */

        /* futureRT() */
        Exc res = r.futureRT();
        try {
            res.nothing();
            bad();
        } catch (RuntimeException re) {
            good();
        }

        /* voidExc() */
        try {
            r.voidExc();
            bad();
        } catch (Exception e) {
            if (e.getMessage().startsWith("Test")) {
                good();
            } else {
                bad();
            }
        }

        /* futureExc() */
        try {
            r.futureExc();
            bad();
        } catch (Exception e) {
            if (e.getMessage().startsWith("Test")) {
                good();
            } else {
                bad();
            }
        }

        testMechanism(r);
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
