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

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class Switch {

    // 1 error - because of the case 2 block, the while instruction
    @MigrationSignal
    public void switchme(Node node, int flag, boolean onCond, String nr) throws MigrationException {

        switch (flag) {
            case 0:
                System.out.println("MIgrating");
                PAMobileAgent.migrateTo(node);
                break;
            case 1:
                try {
                    Integer.parseInt(nr);
                    Object o = nr;
                    Double d = (Double) o;
                    PAMobileAgent.migrateTo(node);
                } catch (NumberFormatException e) {
                    PAMobileAgent.migrateTo(node);
                } catch (ClassCastException e) {
                    org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
                }
                break;
            case 2:
                while (onCond) {
                    System.out.println("Will migrate");
                    org.objectweb.proactive.api.PAMobileAgent.migrateTo(node);
                }
                break;
            default:
                if (onCond) {
                    System.out.println("will migrate");
                    PAMobileAgent.migrateTo(node);
                } else
                    PAMobileAgent.migrateTo(node);
                break;
        }

    }
}
