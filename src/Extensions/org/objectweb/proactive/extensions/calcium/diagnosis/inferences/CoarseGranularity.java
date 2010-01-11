/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.extensions.calcium.diagnosis.inferences;

import org.objectweb.proactive.extensions.calcium.statistics.Stats;


public class CoarseGranularity extends AbstractInference {

    /**
     * @param threshold  The ratio between Tcomp/Toverhead, which corresponds
     * to the granularity should be smaller than this threshold.  A good
     * candidate for this threshold is 10.
     * @param inferences
     */
    public CoarseGranularity(int threshold, Inference... inferences) {
        super(threshold, inferences);
    }

    @Override
    boolean hasSymptom(Stats stats) {
        double overhead = stats.getProcessingTime() - stats.getComputationTime();

        double value = stats.getComputationTime() / overhead;
        if (logger.isDebugEnabled() && (threshold < value)) {
            logger.debug(this.getClass().getSimpleName() + ": " + threshold + " !> " + value);
        }

        if (threshold < value) {
            return true;
        }
        return false;
    }
}
