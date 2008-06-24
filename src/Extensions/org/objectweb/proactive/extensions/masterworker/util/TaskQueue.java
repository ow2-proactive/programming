/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.masterworker.util;

import java.util.LinkedList;
import java.util.Queue;


/**
 * Queue of pending tasks, able to tell if some tasks have been submitted by a given worker
 *
 * @author The ProActive Team
 */
public class TaskQueue extends LinkedList<TaskID> implements Queue<TaskID> {

    public TaskQueue() {
        super();
    }

    public int countTasksByOriginator(String originator) {
        int count = 0;
        for (TaskID tid : this) {
            if (tid.getOriginator().equals(originator)) {
                count++;
            }
        }

        return count;
    }

    public boolean hasTasksByOriginator(String originator) {
        for (TaskID tid : this) {
            if (tid.getOriginator().equals(originator)) {
                return true;
            }
        }
        return false;
    }

}
