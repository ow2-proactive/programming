/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.documentation.exceptions;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.examples.documentation.classes.A;


/**
 * @author The ProActive Team
 *
 */
public class ExceptionHandling {

    public static void main(String[] args) {
        //@snippet-start basic_exception
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        try {
            A a = PAActiveObject.newActive(A.class, null);
            a.throwsException(true); //Synchronous method due to the potential exception
            System.out.println("Hello");
            //...
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        //@snippet-end basic_exception

        //@snippet-start tryWithCatch_exception
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        PAException.tryWithCatch(Exception.class);
        try {
            A a = PAActiveObject.newActive(A.class, null);
            a.throwsException(true); // Asynchronous method call that can throw an exception
            System.out.println("Hello");
            //...
            PAException.endTryWithCatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PAException.removeTryWithCatch();
        }
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        //@snippet-end tryWithCatch_exception

        //@snippet-start tryWithCatch_throwArrivedException
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        PAException.tryWithCatch(Exception.class);
        try {
            A a = PAActiveObject.newActive(A.class, null);
            a.throwsException(true); // Asynchronous method call that can throw an exception
            //...
            // Throws exceptions which has been already raised by active object
            PAException.throwArrivedException();

            // Should appear since the exception did not have the time
            // to be thrown (due to the sleep(5000))
            System.out.println("Hello");
            //...
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Throws exceptions which has been already raised by active object
            PAException.throwArrivedException();

            // Should not appear since the exception had the time to
            // be thrown.
            System.out.println("Hello");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            PAException.endTryWithCatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PAException.removeTryWithCatch();
        }
        //@snippet-end tryWithCatch_throwArrivedException

        //@snippet-start tryWithCatch_waitForPotentialException
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        PAException.tryWithCatch(Exception.class);
        try {
            A a = PAActiveObject.newActive(A.class, null);
            a.throwsException(true); // Asynchronous method call that can throw an exception
            //...
            // At that moment, we want to be sure that no exception has been
            // raised by the code before.
            PAException.waitForPotentialException();
            //...
            // Should not appear since waitForPotentialException
            // is blocking.
            System.out.println("Hello");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            PAException.endTryWithCatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PAException.removeTryWithCatch();
        }
        //@snippet-end tryWithCatch_waitForPotentialException

    }
}
