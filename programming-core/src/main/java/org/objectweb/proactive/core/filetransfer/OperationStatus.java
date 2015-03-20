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
package org.objectweb.proactive.core.filetransfer;

import java.io.IOException;
import java.io.Serializable;


/**
 * This class is used to determine if an operation finished successfully or with
 * an exception.
 *
 * @author The ProActive Team
 */
public class OperationStatus implements Serializable {

    private static final long serialVersionUID = 60L;
    private IOException e = null;

    /**
     * ProActive empty constructor.
     * Can also be used if the operation finished successfully
     */
    public OperationStatus() { //default is successful operation
    }

    /**
     * This constructor can be used if the operation encountered problems
     * while processing.
     * @param e The exception that was encountered
     */
    public OperationStatus(IOException e) { //operation encountered problems
        this.e = e;
    }

    /**
     * Determines if the operation had problems
     * @return true if problems where encountered
     */
    public boolean hasException() {
        return e != null;
    }

    /**
     * Returns the exception that was encountered
     * @return The exception or null if no exception took place.
     */
    public IOException getException() {
        return e;
    }
}
