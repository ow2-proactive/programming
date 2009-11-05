/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.annotations.migrationsignal.inputs;

import java.rmi.AlreadyBoundException;
import java.util.Random;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class ErrorNotLast {

    // error - not last statement
    @MigrationSignal
    public void migrateTo1() throws MigrationException, NodeException, AlreadyBoundException {
        int i = 0;
        PAMobileAgent.migrateTo(NodeFactory.createNode(""));
        i++; // muhahaw
    }

    // obvious error
    @MigrationSignal
    public void migrateTo2() throws MigrationException, NodeException, AlreadyBoundException {
        int i = 0;
        PAMobileAgent.migrateTo(NodeFactory.createNode(""));
        i++; // muhahaw
        System.out.println("Migration done!");
    }

    // a more subtle error - the return statement actually contains 
    // another method call - the call to the Integer constructor
    @MigrationSignal
    public Integer migrateTo3() throws MigrationException, NodeException, AlreadyBoundException {
        int i = 0;
        org.objectweb.proactive.api.PAMobileAgent.migrateTo(NodeFactory.createNode(""));
        return new Integer(i);
    }

    // error - another method call after migrateTo call
    @MigrationSignal
    public int migrateTo4() throws MigrationException, NodeException, AlreadyBoundException {
        Random r = new Random();
        org.objectweb.proactive.api.PAMobileAgent.migrateTo(NodeFactory.createNode(""));
        return r.nextInt();
    }

}
