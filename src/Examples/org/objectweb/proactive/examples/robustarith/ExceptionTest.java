/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.robustarith;

import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


class DangerousException extends Exception {
    DangerousException(String str) {
        super(str);
    }
}

@ActiveObject
public class ExceptionTest implements Serializable {

    /* Empty constructor for ProActive */
    public ExceptionTest() {
    }

    public ExceptionTest dangerousMethod() throws DangerousException {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new DangerousException("Very dangerous");
    }

    public static void main(String[] args) {
        ExceptionTest test = null;
        try {
            test = PAActiveObject.newActive(ExceptionTest.class, null);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        PAException.tryWithCatch(DangerousException.class);
        try {
            System.out.println("Appel");
            ExceptionTest et = test.dangerousMethod();

            //            et.toString();
            System.out.println("Fin de l'appel");
            PAException.endTryWithCatch();
        } catch (DangerousException de) {
            System.out.println("Backtrace de l'exception :");
            de.printStackTrace(System.out);
        } finally {
            PAException.removeTryWithCatch();
        }
        System.out.println("fini");
    }
}
