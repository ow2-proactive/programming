/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.annotations.migrationsignal.inputs;

import static org.objectweb.proactive.api.PAMobileAgent.migrateTo;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class TryCatchFinally {

    @MigrationSignal
    public void migrateTryCatch(Node node, String nr) throws MigrationException {
        try {
            Integer.parseInt(nr);
            Object o = nr;
            Double d = (Double) o;
            PAMobileAgent.migrateTo(node);
        } catch (NumberFormatException e) {
            migrateTo(node);
        } catch (ClassCastException e) {
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        }
    }

    @MigrationSignal
    public void migrateTryCatchFinallyGood(Node node, String nr) throws MigrationException {
        try {
            Integer.parseInt(nr);
            Object o = nr;
            Double d = (Double) o;
        } catch (NumberFormatException e) {
            System.out.println("I want number not " + nr);
        } catch (ClassCastException e) {
            System.out.println("WTF?!");
        } finally {
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        }

    }

    @MigrationSignal
    public void migrateTryCatchFinallyNo(Node node, String nr) throws MigrationException {
        try {
            Integer.parseInt(nr);
            Object o = nr;
            Double d = (Double) o;
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        } catch (NumberFormatException e) {
            System.out.println("I want number not " + nr);
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        } catch (ClassCastException e) {
            System.out.println("WTF?!");
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
        } finally {
            System.out.println("Here should be migrateTo");
        }

    }

}
