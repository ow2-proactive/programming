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
package org.objectweb.proactive.extensions.calcium.task;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class TaskPriority implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS);
    public static int DEFAULT_PRIORITY = 0;
    public static int DEFAULT_INTRA_FAMILY_PRIORITY = 0;
    int priority; //higher number => higher priority
    int intraFamilyPri; //higher number => higher priority

    public TaskPriority(int priority, int intraFamilyPriority) {
        this.priority = priority;
        this.intraFamilyPri = intraFamilyPriority;
    }

    public TaskPriority(int priority) {
        this(priority, DEFAULT_INTRA_FAMILY_PRIORITY);
    }

    public TaskPriority() {
        this(DEFAULT_PRIORITY, DEFAULT_INTRA_FAMILY_PRIORITY);
    }

    public void setDefaultIntraFamilyPriority() {
        this.intraFamilyPri = DEFAULT_INTRA_FAMILY_PRIORITY;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int newPriority) {
        this.priority = newPriority;
    }

    public void setIntraFamilyPri(int intraFamilyPri) {
        this.intraFamilyPri = intraFamilyPri;
    }

    public int getIntraFamilyPri() {
        return intraFamilyPri + 1;
    }

    public TaskPriority getNewChildPriority() {
        return new TaskPriority(getPriority(), getIntraFamilyPri());
    }
}
