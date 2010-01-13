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
package org.objectweb.proactive.extensions.masterworker.interfaces.internal;

import java.io.Serializable;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * Internal view of a task in the Master/Worker API<br/>
 * Adds the possibility to set the result for a task<br/>
 * Adds the notion of a "Task ID"<br/>
 * @author The ProActive Team
 *
 * @param <R>
 */
public interface ResultIntern<R extends Serializable> extends Identifiable, Serializable {

    /**
     * get the result of the task
     * @return the result
     */
    R getResult();

    /**
     * sets the result of the task
     * @param res the result
     */
    void setResult(R res);

    /**
     * tells if the task threw a functional exception
     * @return answer
     */
    boolean threwException();

    /**
     * returns the actual functional exception thrown by the task
     * @return the exception
     */
    Throwable getException();

    /**
     * sets the exception thrown by the task
     * @param e the exception
     */
    void setException(Throwable e);
}
