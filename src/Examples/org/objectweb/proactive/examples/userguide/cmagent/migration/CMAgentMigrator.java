//@tutorial-start
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
//@snippet-start migrate_cma_skeleton
//@snippet-start migrate_cma_full
package org.objectweb.proactive.examples.userguide.cmagent.migration;

import java.io.Serializable;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


@ActiveObject
public class CMAgentMigrator extends CMAgentInitialized implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;

    @MigrationSignal
    public void migrateTo(Node whereTo) {
        try {
            //TODO 1. Migrate the active object to the Node received as parameter
            //@snippet-break migrate_cma_skeleton
            //@tutorial-break
            //should be the last call in this method
            //instructions after a call to PAMobileAgent.migrateTo are NOT executed 
            PAMobileAgent.migrateTo(whereTo);
            //@snippet-resume migrate_cma_skeleton
            //@tutorial-resume
        } catch (ProActiveException moveExcep) {
            System.err.println(moveExcep.getMessage());
        }
    }
}
//@snippet-end migrate_cma_skeleton
//@snippet-end migrate_cma_full
//@tutorial-end
