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
package functionalTests.activeobject.migration.strategy;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.migration.MigrationStrategy;
import org.objectweb.proactive.core.migration.MigrationStrategyImpl;
import org.objectweb.proactive.core.migration.MigrationStrategyManager;
import org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl;


public class A implements Serializable, RunActive {

    private static final long serialVersionUID = 52;

    /**
     *
     */
    int counter = 0;
    private MigrationStrategyManager migrationStrategyManager;
    private MigrationStrategy migrationStrategy;

    public A() {
    }

    public A(String[] nodesUrl) {
        migrationStrategy = new MigrationStrategyImpl();
        int i;
        for (i = 0; i < nodesUrl.length; i++) {
            migrationStrategy.add(nodesUrl[i], "arrived");
        }
    }

    public void runActivity(Body body) {
        if (counter == 0) {
            try {
                migrationStrategyManager = new MigrationStrategyManagerImpl((Migratable) body);
                migrationStrategyManager.onDeparture("leaving");
                migrationStrategyManager.setMigrationStrategy(this.migrationStrategy);
                migrationStrategyManager.startStrategy(body);
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter++;
        }
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        service.fifoServing();
    }

    public void leaving() {
        counter++;
    }

    public void arrived() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }
}
