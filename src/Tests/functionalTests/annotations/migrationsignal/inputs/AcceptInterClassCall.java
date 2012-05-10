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
package functionalTests.annotations.migrationsignal.inputs;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;

import functionalTests.annotations.migrationsignal.inputs.inter.MigrationProvider;


@ActiveObject
public class AcceptInterClassCall {

    private AnotherMigrateTo ao = new AnotherMigrateTo();
    private MigrationProvider mig = new MigrationProvider();

    // call against a local variable
    @MigrationSignal
    public void migrateTo1() throws MigrationException {
        AnotherMigrateTo amt = new AnotherMigrateTo();
        amt.migrateTo();
    }

    //    @MigrationSignal
    //    public void migrateTo2() throws MigrationException {
    //        // a more sophisticated form of call
    //        new AnotherMigrateTo().migrateTo();
    //    }

    // local variable; type fully named
    @MigrationSignal
    public void migrateTo2() throws ProActiveException {
        functionalTests.annotations.migrationsignal.inputs.inter.MigrationProvider mp = new MigrationProvider();
        mp.migrateTo();
    }

    // local variable; type imported
    @MigrationSignal
    public void migrateTo3() throws ProActiveException {
        MigrationProvider mp = new MigrationProvider();
        mp.migrateTo();
    }

    // call against a field member, class in the same compilation unit
    @MigrationSignal
    public void migrateTo4() throws MigrationException {
        ao.migrateTo();
    }

    // call against a field member, class in different package
    @MigrationSignal
    public void migrateTo5() throws MigrationException {
        mig.migrateTo();
    }

}

class AnotherMigrateTo {

    @MigrationSignal
    public void migrateTo() throws MigrationException {
        PAMobileAgent.migrateTo(new Object());
    }
}
