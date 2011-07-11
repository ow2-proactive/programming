/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionalTests.activeobject.migration.loopmigration;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;


public class A implements Serializable, RunActive {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    /**
     *
     */
    public static final int MAX_MIG = 20;
    String node1;
    String node2;
    boolean exceptionThrown = false;
    int migrationCounter = 0;
    boolean isNode1 = true;

    public A() {
    }

    public A(String node1, String node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    protected boolean inNode1() {
        return isNode1;
    }

    protected void changeNode() {
        isNode1 = !isNode1;
        migrationCounter++;
    }

    public void runActivity(Body body) {
        if (migrationCounter < MAX_MIG) {
            try {
                if (inNode1()) {
                    changeNode();
                    PAMobileAgent.migrateTo(node2);
                } else {
                    changeNode();
                    PAMobileAgent.migrateTo(node1);
                }
            } catch (MigrationException e) {
                this.exceptionThrown = true;
                e.printStackTrace();
            }
        } else {
            Service service = new Service(body);
            service.blockingServeOldest();
        }
    }

    public boolean isException() {
        return exceptionThrown;
    }
}
